package com.unbidden.jvtaskmanagementsystem.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtUtil {
    private static final Logger LOGGER = LogManager.getLogger(JwtUtil.class);

    private static final int REFRESH_TOKEN_BYTE_SIZE = 32;

    private final SecretKey secret;

    @Autowired
    private SecureRandom secureRandom;

    @Autowired
    private MessageDigest messageDigest;

    @Autowired
    private Base64.Encoder base64UrlEncoder;

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
            return false;
        }
    }

    @NonNull 
    public String getUsername(@NonNull String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    @NonNull
    public String generateRefreshToken() {
        byte[] randomBytes = new byte[REFRESH_TOKEN_BYTE_SIZE];
        secureRandom.nextBytes(randomBytes);
        return base64UrlEncoder.encodeToString(randomBytes);
    }

    @NonNull
    public String hashRefreshToken(@NonNull String refreshToken) {
        byte[] hash = messageDigest.digest(refreshToken.getBytes(StandardCharsets.UTF_8));
        return base64UrlEncoder.encodeToString(hash);
    }

    @NonNull
    public Cookie getRefreshTokenCookie(@NonNull String refreshToken) {
        final Cookie cookie = new Cookie("refresh_token", refreshToken);

        cookie.setHttpOnly(true);
        // cookie.setSecure(true);
        cookie.setPath("/api/auth");
        cookie.setAttribute("SameSite", "Lax");
        return cookie;
    }

    @NonNull
    public Cookie getAccessTokenCookie(@NonNull String accessToken) {
        final Cookie cookie = new Cookie("access_token", accessToken);

        cookie.setHttpOnly(true);
        // cookie.setSecure(true);
        cookie.setPath("/api");
        cookie.setAttribute("SameSite", "Lax");
        return cookie;
    }

    @NonNull
    public Cookie getRefreshTokenRemoveCookie() {
        final Cookie cookie = new Cookie("refresh_token", "");

        cookie.setHttpOnly(true);
        // cookie.setSecure(true);
        cookie.setPath("/api/auth");
        cookie.setAttribute("SameSite", "Lax");
        cookie.setMaxAge(0);
        return cookie;
    }

    @NonNull
    public Cookie getAccessTokenRemoveCookie() {
        final Cookie cookie = new Cookie("access_token", "");

        cookie.setHttpOnly(true);
        // cookie.setSecure(true);
        cookie.setPath("/api");
        cookie.setAttribute("SameSite", "Lax");
        cookie.setMaxAge(0);
        return cookie;
    }

    @NonNull
    public Optional<String> getAccessTokenFromCookie(@NonNull HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    return Optional.of(cookie.getValue());
                }
            }
        }
        return Optional.empty();
    }

    @NonNull
    public Optional<String> getRefreshTokenFromCookie(@NonNull HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if ("refresh_token".equals(cookie.getName())) {
                    return Optional.of(cookie.getValue());
                }
            }
        }
        return Optional.empty();
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
