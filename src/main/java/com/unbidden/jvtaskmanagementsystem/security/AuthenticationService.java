package com.unbidden.jvtaskmanagementsystem.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unbidden.jvtaskmanagementsystem.dto.ErrorResponse;
import com.unbidden.jvtaskmanagementsystem.dto.auth.LoginRequestDto;
import com.unbidden.jvtaskmanagementsystem.exception.ErrorType;
import com.unbidden.jvtaskmanagementsystem.exception.UnexpectedException;
import com.unbidden.jvtaskmanagementsystem.model.RefreshToken;
import com.unbidden.jvtaskmanagementsystem.repository.RefreshJwtRepository;
import com.unbidden.jvtaskmanagementsystem.repository.UserRepository;
import com.unbidden.jvtaskmanagementsystem.util.AuthUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class AuthenticationService {
    private static final Logger LOGGER = LogManager.getLogger(AuthenticationService.class);

    private final AuthenticationManager authManager;

    private final RefreshJwtRepository refreshJwtRepository;

    private final UserRepository userRepository;

    private final CookieCsrfTokenRepository csrfTokenRepository;

    private final ObjectMapper objectMapper;

    private final AuthUtil authUtil;

    @Value("${jwt.refresh-expiration-hours}")
    private Long refreshTokenExpirationHours;

    @Value("${refresh-token.cleanup.max-age-days}")
    private Long refreshTokenMaxAgeDays;

    @Transactional
    public void authenticate(@NonNull LoginRequestDto requestDto, @NonNull HttpServletResponse response) {
        LOGGER.debug("User " + requestDto.getUsername() + " is trying to authenticate.");
        final Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                requestDto.getUsername(), requestDto.getPassword()));
        LOGGER.debug("User " + requestDto.getUsername() + " has been authenticatied. Generating refresh token...");

        final String refreshToken = authUtil.generateRefreshToken();
        refreshJwtRepository.save(createRefreshTokenForUser(requestDto.getUsername(), refreshToken));

        response.addCookie(authUtil.getRefreshTokenCookie(refreshToken));
        response.addCookie(authUtil.getAccessTokenCookie(authUtil.generateToken(authentication.getName())));
    }

    @Transactional
    public void refreshToken(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response) {
        final Optional<String> refreshTokenOpt = authUtil.getRefreshTokenFromCookie(request);

        if (refreshTokenOpt.isEmpty()) {
            writeHttpResponse(response, ErrorType.AUTH_NO_REFRESH_TOKEN, "No refresh token found in request cookies.");
            return;
        }
        final String hashedToken = authUtil.hashRefreshToken(refreshTokenOpt.get());
        final Optional<RefreshToken> refreshJwtOpt = refreshJwtRepository.findByToken(hashedToken);
        if (refreshJwtOpt.isEmpty()) {
            writeHttpResponse(response, ErrorType.AUTH_INVALID_REFRESH_TOKEN, "The refresh token is invalid.");
            return;
        }
        if (refreshJwtOpt.get().getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshJwtRepository.delete(refreshJwtOpt.get());
            writeHttpResponse(response, ErrorType.AUTH_EXPIRED_REFRESH_TOKEN, "The refresh token has expired.");
            return;
        }
        if (refreshJwtOpt.get().getRevokedAt() != null) {
            LOGGER.warn("Revoked refresh token. Possible token reuse attack. Revoking all refresh tokens for user "
                    + refreshJwtOpt.get().getUser().getUsername() + "...");
            refreshJwtRepository.deleteAllByUserId(refreshJwtOpt.get().getUser().getId());
            LOGGER.debug("All refresh tokens for user "
                    + refreshJwtOpt.get().getUser().getUsername() + " have been revoked.");
            writeHttpResponse(response, ErrorType.AUTH_REVOKED_REFRESH_TOKEN, "The refresh token has already been revoked.");
            return;
        }
        response.addCookie(authUtil.getAccessTokenCookie(authUtil.generateToken(refreshJwtOpt.get().getUser().getUsername())));
        refreshJwtOpt.get().setRevokedAt(LocalDateTime.now());
        refreshJwtRepository.save(refreshJwtOpt.get());

        final String newRefreshTokenStr = authUtil.generateRefreshToken();
        refreshJwtRepository.save(createRefreshTokenForUser(refreshJwtOpt.get().getUser().getUsername(), newRefreshTokenStr));
        response.addCookie(authUtil.getRefreshTokenCookie(newRefreshTokenStr));
    }

    @Transactional
    public void logout(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response) {
        final Optional<String> refreshTokenOpt = authUtil.getRefreshTokenFromCookie(request);

        if (refreshTokenOpt.isPresent()) {
            refreshJwtRepository.deleteByToken(refreshTokenOpt.get());
        }
        response.addCookie(authUtil.getAccessTokenRemoveCookie());
        response.addCookie(authUtil.getRefreshTokenRemoveCookie());
    }

    @Transactional
    public void cleanUpRefreshTokens() {
        LOGGER.info("Cleaning up old refresh tokens...");
        final int numberOfDeletions = refreshJwtRepository.deleteByExpiryDateBefore(
                LocalDateTime.now().minusDays(refreshTokenMaxAgeDays));

        LOGGER.info("Cleaned up " + numberOfDeletions + " outdated tokens.");
    }

    public CsrfToken refreshCsrfToken(HttpServletRequest request, HttpServletResponse response) {
        csrfTokenRepository.saveToken(null, request, response);
        final CsrfToken token = csrfTokenRepository.generateToken(request);
        
        csrfTokenRepository.saveToken(token, request, response);
        return token;
    }

    private RefreshToken createRefreshTokenForUser(String username, String refreshTokenStr) {
        final RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(userRepository.findByUsername(username).get());
        refreshToken.setToken(authUtil.hashRefreshToken(refreshTokenStr));
        refreshToken.setExpiryDate(LocalDateTime.now().plusHours(refreshTokenExpirationHours));
        return refreshToken;
    }

    private void writeHttpResponse(HttpServletResponse response, ErrorType type, String message) {
        try {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            objectMapper.writeValue(response.getWriter(), new ErrorResponse(LocalDateTime.now(),
                    type, message));
        } catch (IOException ex) {
            throw new UnexpectedException("An IO Exception occured while writing an error "
                    + "to the HTTP response.", ErrorType.INTERNAL);
        }
    }
}
