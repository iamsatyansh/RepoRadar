package com.reporadar.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reporadar.api.analysis.AnalysisController;
import com.reporadar.api.analysis.AnalysisDto;
import com.reporadar.api.auth.AuthController;
import com.reporadar.api.auth.AuthDto;
import com.reporadar.config.CorsProperties;
import com.reporadar.domain.DomainEnums;
import com.reporadar.error.ApiException;
import com.reporadar.error.ApiExceptionHandler;
import com.reporadar.security.AuthenticatedUser;
import com.reporadar.security.JwtAuthenticationFilter;
import com.reporadar.security.JwtTokenService;
import com.reporadar.security.SecurityConfiguration;
import com.reporadar.service.AnalysisService;
import com.reporadar.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AuthController.class, AnalysisController.class})
@Import({SecurityConfiguration.class, JwtAuthenticationFilter.class, ApiExceptionHandler.class, ApiSecurityMvcTest.MvcTestConfiguration.class})
class ApiSecurityMvcTest {
    private static final UUID USER_ID = UUID.fromString("ab4e23e5-9d0d-4305-9aa5-e204a0ca87ea");

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;
    @MockBean private AuthService authService;
    @MockBean private AnalysisService analysisService;
    @MockBean private JwtTokenService tokenService;

    @Test
    void registersAValidWorkspaceAndReturnsTheExpectedResponseContract() throws Exception {
        AuthDto.UserResponse user = new AuthDto.UserResponse(USER_ID, "signal-user", "signal@example.com");
        when(authService.register(any())).thenReturn(new AuthDto.AuthResponse("signed.jwt", user));

        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new AuthDto.RegisterRequest("signal-user", "signal@example.com", "long-enough-password"))))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value("signed.jwt"))
                .andExpect(jsonPath("$.user.id").value(USER_ID.toString()))
                .andExpect(jsonPath("$.user.username").value("signal-user"));
        verify(authService).register(any(AuthDto.RegisterRequest.class));
    }

    @Test
    void rejectsInvalidRegistrationWithStandardizedValidationProblem() throws Exception {
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"invalid space\",\"email\":\"not-an-email\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void mapsDuplicateRegistrationToConflictProblemWithoutLeakingSecurityDetails() throws Exception {
        when(authService.register(any())).thenThrow(new ApiException(HttpStatus.CONFLICT, "EMAIL_IN_USE", "An account already exists for this email."));

        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new AuthDto.RegisterRequest("signal-user", "signal@example.com", "long-enough-password"))))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("EMAIL_IN_USE"))
                .andExpect(jsonPath("$.detail").value("An account already exists for this email."));
    }

    @Test
    void deniesUnauthenticatedAnalysisAccessWithProblemResponse() throws Exception {
        mvc.perform(get("/api/v1/analyses"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void listsOnlyTheAuthenticatedUsersAnalysisRecords() throws Exception {
        when(analysisService.list(USER_ID)).thenReturn(List.of(sampleAnalysis()));

        mvc.perform(get("/api/v1/analyses").with(SecurityMockMvcRequestPostProcessors.user(authenticatedUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("9cf8b1ca-1f2e-43fb-9567-2ae9eaf58bea"))
                .andExpect(jsonPath("$[0].repository.fullName").value("spring-projects/spring-boot"))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"));
        verify(analysisService).list(USER_ID);
    }

    @Test
    void mapsMalformedRepositoryErrorsFromTheOwnedAnalysisFlow() throws Exception {
        when(analysisService.create(eq(USER_ID), any())).thenThrow(new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REPOSITORY_URL", "Provide a canonical public GitHub repository URL, for example https://github.com/owner/repository."));

        mvc.perform(post("/api/v1/analyses").with(SecurityMockMvcRequestPostProcessors.user(authenticatedUser()))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"repositoryUrl\":\"https://gitlab.com/owner/repository\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("INVALID_REPOSITORY_URL"));
        verify(analysisService).create(eq(USER_ID), any(AnalysisDto.CreateAnalysisRequest.class));
    }

    private static AuthenticatedUser authenticatedUser() { return new AuthenticatedUser(USER_ID, "signal-user", "signal@example.com", DomainEnums.UserRole.USER); }

    private static AnalysisDto.AnalysisResponse sampleAnalysis() {
        return new AnalysisDto.AnalysisResponse(UUID.fromString("9cf8b1ca-1f2e-43fb-9567-2ae9eaf58bea"), DomainEnums.AnalysisStatus.COMPLETED, Instant.parse("2026-08-26T16:00:00Z"), Instant.parse("2026-08-26T16:00:03Z"), new AnalysisDto.RepositoryResponse("spring-projects", "spring-boot", "spring-projects/spring-boot", "https://github.com/spring-projects/spring-boot", "Framework", 1, 2, 3, 4, "Java", "main", Instant.parse("2026-08-25T00:00:00Z"), Instant.parse("2014-04-01T00:00:00Z")), "abc123", List.of(), List.of(), List.of(), List.of(), List.of());
    }

    @TestConfiguration
    static class MvcTestConfiguration {
        @Bean CorsProperties corsProperties() { return new CorsProperties("http://localhost:3000"); }
    }
}
