package com.unbidden.jvtaskmanagementsystem.service.oauth2.resolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.ThirdPartyOperationResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.ThirdPartyOperationResult.ThirdPartyOperationStatus;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.AccountResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.DropboxOperationResult;
import com.unbidden.jvtaskmanagementsystem.exception.oauth2.OAuth2ExternalIdException;
import com.unbidden.jvtaskmanagementsystem.model.ClientRegistration;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.repository.oauth2.ClientRegistrationRepository;
import com.unbidden.jvtaskmanagementsystem.service.DropboxService;

@Component
public class DropboxExternalIdResolver implements ExternalIdResolver {
    private final DropboxService dropboxService;

    private final ClientRegistration clientRegistration;

    public DropboxExternalIdResolver(@Lazy @Autowired DropboxService dropboxService,
            @Autowired ClientRegistrationRepository registrationRepository) {
        this.dropboxService = dropboxService;
        this.clientRegistration = registrationRepository.findByClientName("dropbox").get();
    }

    @Override
    public String resolveId(@NonNull User user, @NonNull String token)
            throws OAuth2ExternalIdException {
        final AccountResult result = dropboxService.getUserAccount(user, token);

        if (result.getStatus() != ThirdPartyOperationResult.ThirdPartyOperationStatus.SUCCESS) {
            throw new OAuth2ExternalIdException("Failed to fetch Dropbox account details for user "
                    + user.getUsername() + ". Status: " + result.getStatus() + ".");
        }
        return result.getAccount().getAccountId();
    }

    @Override
    public void revokeToken(@NonNull User user, @NonNull String token)
            throws OAuth2ExternalIdException {
        final DropboxOperationResult result = dropboxService.logout(user, token);
        
        if (result.getStatus() != ThirdPartyOperationStatus.SUCCESS) {
            throw new OAuth2ExternalIdException("Failed to revoke Dropbox token for user "
                    + user.getUsername() + ". Status: " + result.getStatus() + ".");
        }
    }

    @Override
    public ClientRegistration getClientRegistration() {
        return clientRegistration;
    }
}
