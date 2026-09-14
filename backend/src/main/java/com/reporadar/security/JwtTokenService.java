package com.reporadar.security;

import com.reporadar.config.JwtProperties;
import com.reporadar.domain.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtTokenService {
    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtTokenService(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(properties.secret()));
    }

    public String createAccessToken(UserAccount user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(properties.issuer())
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.accessTokenTtl())))
                .signWith(signingKey)
                .compact();
    }

    public AuthenticatedUser parse(String token) {
        Claims claims = Jwts.parser().verifyWith(signingKey).requireIssuer(properties.issuer()).build().parseSignedClaims(token).getPayload();
        return new AuthenticatedUser(UUID.fromString(claims.getSubject()), claims.get("username", String.class), claims.get("email", String.class), com.reporadar.domain.DomainEnums.UserRole.valueOf(claims.get("role", String.class)));
    }
}
