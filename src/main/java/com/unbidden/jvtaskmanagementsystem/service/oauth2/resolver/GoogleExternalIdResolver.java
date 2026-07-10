package com.unbidden.jvtaskmanagementsystem.service.oauth2.resolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.ThirdPartyOperationResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.calendar.CalendarOperationResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.calendar.EmailResult;
import com.unbidden.jvtaskmanagementsystem.exception.oauth2.OAuth2ExternalIdException;
import com.unbidden.jvtaskmanagementsystem.model.ClientRegistration;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.repository.oauth2.ClientRegistrationRepository;
import com.unbidden.jvtaskmanagementsystem.service.GoogleCalendarService;

@Component
public class GoogleExternalIdResolver implements ExternalIdResolver {
    private final GoogleCalendarService calendarService;

    private final ClientRegistration clientRegistration;

    public GoogleExternalIdResolver(@Lazy @Autowired GoogleCalendarService calendarService,
            @Autowired ClientRegistrationRepository registrationRepository) {
        this.calendarService = calendarService;
        this.clientRegistration = registrationRepository.findByClientName("google").get();
    }

    @Override
    public String resolveId(@NonNull User user, @NonNull String token)
            throws OAuth2ExternalIdException {
        final EmailResult result = calendarService.getEmail(user, token);

        if (result.getStatus() != ThirdPartyOperationResult.ThirdPartyOperationStatus.SUCCESS) {
            throw new OAuth2ExternalIdException("Failed to fetch Google account details for user "
                    + user.getUsername() + ". Status: " + result.getStatus() + ".");
        }
        return result.getEmail();
    }

    @Override
    public void revokeToken(@NonNull User user, @NonNull String token) throws OAuth2ExternalIdException {
        final CalendarOperationResult result = calendarService.logout(user, token);

        if (result.getStatus() != ThirdPartyOperationResult.ThirdPartyOperationStatus.SUCCESS) {
            throw new OAuth2ExternalIdException("Failed to revoke Google access token for user "
                    + user.getUsername() + ". Status: " + result.getStatus() + ".");
        }
    }

    @Override
    public ClientRegistration getClientRegistration() {
        return clientRegistration;
    }
}
