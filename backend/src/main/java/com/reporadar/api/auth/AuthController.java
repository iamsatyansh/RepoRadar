package com.reporadar.api.auth;

import com.reporadar.security.AuthenticatedUser;
import com.reporadar.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    AuthDto.AuthResponse register(@Valid @RequestBody AuthDto.RegisterRequest request) { return authService.register(request); }
    @PostMapping("/login")
    AuthDto.AuthResponse login(@Valid @RequestBody AuthDto.LoginRequest request) { return authService.login(request); }
    @GetMapping("/me")
    AuthDto.UserResponse currentUser(@AuthenticationPrincipal AuthenticatedUser user) { return authService.getUser(user.id()); }
}
