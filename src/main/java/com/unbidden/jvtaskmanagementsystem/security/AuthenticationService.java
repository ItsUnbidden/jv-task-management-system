package com.unbidden.jvtaskmanagementsystem.security;

import java.time.LocalDateTime;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.unbidden.jvtaskmanagementsystem.dto.auth.LoginRequestDto;
import com.unbidden.jvtaskmanagementsystem.model.RefreshToken;
import com.unbidden.jvtaskmanagementsystem.repository.RefreshJwtRepository;
import com.unbidden.jvtaskmanagementsystem.repository.UserRepository;
import com.unbidden.jvtaskmanagementsystem.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class AuthenticationService {
    private static final Logger LOGGER = LogManager.getLogger(AuthenticationService.class);

    private final AuthenticationManager authManager;

    private final RefreshJwtRepository refreshJwtRepository;

    private final UserRepository userRepository;

    private final JwtUtil jwtUtil;

    @Value("${jwt.refresh-expiration-hours}")
    private Long refreshTokenExpirationHours;

    @Transactional
    public void authenticate(@NonNull LoginRequestDto requestDto, @NonNull HttpServletResponse response) {
        LOGGER.info("User " + requestDto.getUsername() + " is trying to authenticate.");
        final Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                requestDto.getUsername(), requestDto.getPassword()));
        LOGGER.info("User " + requestDto.getUsername() + " has been authenticatied. Generating refresh token...");

        final String refreshToken = jwtUtil.generateRefreshToken();
        refreshJwtRepository.save(createRefreshTokenForUser(requestDto.getUsername(), refreshToken));

        response.addCookie(jwtUtil.getRefreshTokenCookie(refreshToken));
        response.addCookie(jwtUtil.getAccessTokenCookie(jwtUtil.generateToken(authentication.getName())));
    }

    @Transactional
    public void refreshToken(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response) {
        final Optional<String> refreshTokenOpt = jwtUtil.getRefreshTokenFromCookie(request);

        if (refreshTokenOpt.isEmpty()) {
            LOGGER.warn("No refresh token found in request cookies.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        final String hashedToken = jwtUtil.hashRefreshToken(refreshTokenOpt.get());
        final Optional<RefreshToken> refreshJwtOpt = refreshJwtRepository.findByToken(hashedToken);
        if (refreshJwtOpt.isEmpty()) {
            LOGGER.warn("Invalid refresh token.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        if (refreshJwtOpt.get().getExpiryDate().isBefore(LocalDateTime.now())) {
            LOGGER.warn("Expired refresh token.");
            refreshJwtRepository.delete(refreshJwtOpt.get());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        if (refreshJwtOpt.get().getRevokedAt() != null) {
            LOGGER.warn("Revoked refresh token. Possible token reuse attack. Revoking all refresh tokens for user "
                    + refreshJwtOpt.get().getUser().getUsername() + "...");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            refreshJwtRepository.deleteAllByUserId(refreshJwtOpt.get().getUser().getId());
            LOGGER.info("All refresh tokens for user "
                    + refreshJwtOpt.get().getUser().getUsername() + " have been revoked.");
            return;
        }
        response.addCookie(jwtUtil.getAccessTokenCookie(jwtUtil.generateToken(refreshJwtOpt.get().getUser().getUsername())));
        refreshJwtOpt.get().setRevokedAt(LocalDateTime.now());
        refreshJwtRepository.save(refreshJwtOpt.get());

        final String newRefreshTokenStr = jwtUtil.generateRefreshToken();
        refreshJwtRepository.save(createRefreshTokenForUser(refreshJwtOpt.get().getUser().getUsername(), newRefreshTokenStr));
        response.addCookie(jwtUtil.getRefreshTokenCookie(newRefreshTokenStr));
    }

    @Transactional
    public void logout(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response) {
        final Optional<String> refreshTokenOpt = jwtUtil.getRefreshTokenFromCookie(request);

        if (refreshTokenOpt.isPresent()) {
            refreshJwtRepository.deleteByToken(refreshTokenOpt.get());
        }
        response.addCookie(jwtUtil.getAccessTokenRemoveCookie());
        response.addCookie(jwtUtil.getRefreshTokenRemoveCookie());
    }

    private RefreshToken createRefreshTokenForUser(String username, String refreshTokenStr) {
        final RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(userRepository.findByUsername(username).get());
        refreshToken.setToken(jwtUtil.hashRefreshToken(refreshTokenStr));
        refreshToken.setExpiryDate(LocalDateTime.now().plusHours(refreshTokenExpirationHours));
        return refreshToken;
    }
}
