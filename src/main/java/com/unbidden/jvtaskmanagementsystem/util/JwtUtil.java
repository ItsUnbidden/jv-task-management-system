package com.unbidden.jvtaskmanagementsystem.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
    private static final Logger LOGGER = LogManager.getLogger(JwtUtil.class);

    private SecretKey secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    public JwtUtil(@Value("${jwt.secret}") String secretString) {
        secret = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
    }

    @NonNull
    public String generateToken(@NonNull String username) {
        LOGGER.info("Generating JWT for user " + username);
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secret)
                .compact();
    }

    @NonNull    
    public boolean isValidToken(@NonNull String token) {
        LOGGER.info("Verifying token...");
        try {
            Jws<Claims> claimsJws = Jwts.parser()
                    .verifyWith(secret)
                    .build()
                    .parseSignedClaims(token);
            return !claimsJws.getPayload().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            throw new BadCredentialsException("Expired or invalid JWT token", e);
        }
    }

    @NonNull 
    public String getUsername(@NonNull String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parser()
                .verifyWith(secret)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }
}
