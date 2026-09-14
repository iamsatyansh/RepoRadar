package com.reporadar.api.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public final class AuthDto {
    private AuthDto() { }
    public record RegisterRequest(@NotBlank @Size(min = 3, max = 64) @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9_-]*$", message = "must contain only letters, numbers, underscores, or hyphens") String username, @NotBlank @Email @Size(max = 320) String email, @NotBlank @Size(min = 12, max = 128) String password) { }
    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) { }
    public record UserResponse(UUID id, String username, String email) { }
    public record AuthResponse(String accessToken, UserResponse user) { }
}
