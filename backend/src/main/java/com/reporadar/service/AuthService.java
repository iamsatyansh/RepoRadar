package com.reporadar.service;

import com.reporadar.api.auth.AuthDto;
import com.reporadar.domain.UserAccount;
import com.reporadar.error.ApiException;
import com.reporadar.repository.UserAccountRepository;
import com.reporadar.security.JwtTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {
    private final UserAccountRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService tokens;

    public AuthService(UserAccountRepository users, PasswordEncoder passwordEncoder, JwtTokenService tokens) { this.users = users; this.passwordEncoder = passwordEncoder; this.tokens = tokens; }

    @Transactional
    public AuthDto.AuthResponse register(AuthDto.RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String username = request.username().trim();
        if (users.existsByEmailIgnoreCase(email)) throw new ApiException(HttpStatus.CONFLICT, "EMAIL_ALREADY_REGISTERED", "An account already exists for this email address.");
        if (users.existsByUsernameIgnoreCase(username)) throw new ApiException(HttpStatus.CONFLICT, "USERNAME_ALREADY_REGISTERED", "This username is already in use.");
        UserAccount user = users.save(new UserAccount(username, email, passwordEncoder.encode(request.password())));
        return responseFor(user);
    }

    @Transactional(readOnly = true)
    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        UserAccount user = users.findByEmailIgnoreCase(request.email().trim()).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Email or password is incorrect."));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Email or password is incorrect.");
        return responseFor(user);
    }

    @Transactional(readOnly = true)
    public AuthDto.UserResponse getUser(UUID id) {
        UserAccount user = users.findById(id).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND", "The authenticated user no longer exists."));
        return userResponse(user);
    }

    private AuthDto.AuthResponse responseFor(UserAccount user) { return new AuthDto.AuthResponse(tokens.createAccessToken(user), userResponse(user)); }
    private AuthDto.UserResponse userResponse(UserAccount user) { return new AuthDto.UserResponse(user.getId(), user.getUsername(), user.getEmail()); }
}
