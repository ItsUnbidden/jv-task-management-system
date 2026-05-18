package com.unbidden.jvtaskmanagementsystem.security;

import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenCleanupService {
    private final AuthenticationService authService;

    @Scheduled(fixedRateString = "${refresh-token.cleanup.interval}",
            initialDelayString = "${refresh-token.cleanup.initial-delay}",
            timeUnit = TimeUnit.HOURS)
    public void cleanUp() {
        authService.cleanUpRefreshTokens();
    }
}
