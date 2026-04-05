package com.pharma.service;

import com.pharma.entity.User;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class TokenService {

    @ConfigProperty(name = "jwt.access.token.expiry", defaultValue = "3600")
    Long accessTokenExpiry;

    @ConfigProperty(name = "jwt.refresh.token.expiry", defaultValue = "604800")
    Long refreshTokenExpiry;

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    public String generateAccessToken(User user) {
        Set<String> roles = new HashSet<>();
        roles.add(user.role);

        return Jwt.issuer(issuer)
                .upn(user.email)
                .groups(roles)
                .claim("userId", user.id)
                .claim("firstName", user.firstName)
                .claim("lastName", user.lastName)
                .claim("countryId", user.country != null ? user.country.id : null)
                .expiresIn(Duration.ofSeconds(accessTokenExpiry))
                .sign();
    }

    public String generateRefreshToken(User user) {
        return Jwt.issuer(issuer)
                .upn(user.email)
                .claim("userId", user.id)
                .claim("type", "refresh")
                .expiresIn(Duration.ofSeconds(refreshTokenExpiry))
                .sign();
    }
}
