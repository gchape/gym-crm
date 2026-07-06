package tech.provokedynamic.gymcrm.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;
import tech.provokedynamic.gymcrm.config.JwtProperties;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@NullMarked
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;
    private final TokenBlacklist tokenBlacklist;

    public String generateToken(String username) {
        var now = new Date();
        var expiry = new Date(now.getTime() + jwtProperties.expiration());

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey())
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(secretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean isValid(String token) {
        try {
            if (tokenBlacklist.isBlacklisted(token)) return false;
            Jwts.parser().verifyWith(secretKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private SecretKey secretKey() {
        return Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }
}
