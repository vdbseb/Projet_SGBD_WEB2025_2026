package be.angularpadelclub.Service;

import be.angularpadelclub.Entity.AdministrateurEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationMinutes;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-minutes:120}") long expirationMinutes
    ) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException(
                    "La clé JWT doit contenir au moins 32 caractères."
            );
        }

        this.secretKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
        this.expirationMinutes = expirationMinutes;
    }

    public String generateAdminToken(AdministrateurEntity admin) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(expirationMinutes * 60);

        Integer siteId = admin.getSite() != null
                ? admin.getSite().getId()
                : null;

        return Jwts.builder()
                .subject(admin.getMatricule())
                .claim("adminId", admin.getId())
                .claim("role", admin.getTypeAdmin())
                .claim("siteId", siteId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException exception) {
            throw new JwtException("Token JWT invalide ou expiré.");
        }
    }

    public String extractMatricule(String token) {
        return parseToken(token).getSubject();
    }

    public String extractRole(String token) {
        Object role = parseToken(token).get("role");
        return role != null ? role.toString() : null;
    }

    public Integer extractAdminId(String token) {
        Object adminId = parseToken(token).get("adminId");

        if (adminId instanceof Integer value) {
            return value;
        }

        if (adminId instanceof Number value) {
            return value.intValue();
        }

        return null;
    }

    public Integer extractSiteId(String token) {
        Object siteId = parseToken(token).get("siteId");

        if (siteId == null) {
            return null;
        }

        if (siteId instanceof Integer value) {
            return value;
        }

        if (siteId instanceof Number value) {
            return value.intValue();
        }

        return null;
    }
}