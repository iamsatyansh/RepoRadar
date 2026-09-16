package com.reporadar.service;

import com.reporadar.api.auth.AuthDto;
import com.reporadar.config.JwtProperties;
import com.reporadar.domain.BaseEntity;
import com.reporadar.domain.UserAccount;
import com.reporadar.error.ApiException;
import com.reporadar.repository.UserAccountRepository;
import com.reporadar.security.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    private static final String SECRET = "test-only-jwt-secret-not-a-real-secret";
    @Mock private UserAccountRepository users;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();
    private final JwtTokenService tokens = new JwtTokenService(new JwtProperties("reporadar-test", SECRET, Duration.ofHours(1)));
    private AuthService service;

    @BeforeEach
    void setUp() { service = new AuthService(users, encoder, tokens); }

    @Test
    void registerNormalizesEmailHashesPasswordAndReturnsParseableJwt() {
        when(users.existsByEmailIgnoreCase("radar@example.com")).thenReturn(false);
        when(users.existsByUsernameIgnoreCase("radar-user")).thenReturn(false);
        when(users.save(any(UserAccount.class))).thenAnswer(invocation -> {
            UserAccount account = invocation.getArgument(0);
            Field id = BaseEntity.class.getDeclaredField("id");
            id.setAccessible(true);
            id.set(account, UUID.randomUUID());
            return account;
        });

        AuthDto.AuthResponse response = service.register(new AuthDto.RegisterRequest("radar-user", " Radar@Example.com ", "a-long-test-password"));

        ArgumentCaptor<UserAccount> saved = ArgumentCaptor.forClass(UserAccount.class);
        org.mockito.Mockito.verify(users).save(saved.capture());
        assertThat(saved.getValue().getEmail()).isEqualTo("radar@example.com");
        assertThat(encoder.matches("a-long-test-password", saved.getValue().getPasswordHash())).isTrue();
        assertThat(tokens.parse(response.accessToken()).email()).isEqualTo("radar@example.com");
    }

    @Test
    void loginRejectsIncorrectCredentialsWithoutRevealingWhichValueFailed() {
        UserAccount user = new UserAccount("radar-user", "radar@example.com", encoder.encode("correct-password"));
        when(users.findByEmailIgnoreCase("radar@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.login(new AuthDto.LoginRequest("radar@example.com", "wrong-password")))
                .isInstanceOf(ApiException.class)
                .hasMessage("Email or password is incorrect.");
    }
}
