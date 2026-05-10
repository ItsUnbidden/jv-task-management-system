package com.unbidden.jvtaskmanagementsystem.service.oauth2.resolver;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.unbidden.jvtaskmanagementsystem.dto.oauth2.OAuth2TokenResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.ThirdPartyOperationResult.ThirdPartyOperationStatus;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.AccountResult;
import com.unbidden.jvtaskmanagementsystem.exception.ErrorType;
import com.unbidden.jvtaskmanagementsystem.exception.StateCollisionException;
import com.unbidden.jvtaskmanagementsystem.model.OAuth2AuthorizedClient;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.repository.oauth2.AuthorizedClientRepository;
import com.unbidden.jvtaskmanagementsystem.service.DropboxService;
import com.unbidden.jvtaskmanagementsystem.util.EntityUtil;

// TODO: remove this entire nonsense.
@Component
public class OAuth2AuthorizedClientDropboxResolver extends OAuth2AuthorizedClientAbstractResolver {
    private static final Logger LOGGER =
            LogManager.getLogger(OAuth2AuthorizedClientDropboxResolver.class);

    @Lazy
    @Autowired
    private DropboxService dropboxService;

    public OAuth2AuthorizedClientDropboxResolver(@Autowired EntityUtil entityUtil,
            @Autowired AuthorizedClientRepository authorizedClientRepository) {
        super(entityUtil.getClientRegistrationByName("dropbox"), authorizedClientRepository);
    }

    @Override
    public OAuth2AuthorizedClient resolveAuthorizedClient(User user,
            OAuth2TokenResponseDto tokenData,
            Optional<OAuth2AuthorizedClient> authorizedClientOpt) {
        LOGGER.debug("Calling super method for user " + user.getId());
        OAuth2AuthorizedClient authorizedClient =
                super.resolveAuthorizedClient(user, tokenData, authorizedClientOpt);
        
        LOGGER.debug("Setting external account id for user " + user.getId());
        final AccountResult dropboxResult = dropboxService.getUserAccount(user);

        if (dropboxResult.getStatus().equals(ThirdPartyOperationStatus.SUCCESS)) {
            authorizedClient.setExternalAccountId(dropboxResult.getAccount().getAccountId());
        }
        Optional<OAuth2AuthorizedClient> authClientWithCurrentExtIdOpt =
                authorizedClientRepository.findByExternalAccountId(authorizedClient
                .getExternalAccountId());
        LOGGER.debug("Testing whether the external id is taken.");
        if (authClientWithCurrentExtIdOpt.isPresent()
                && !authClientWithCurrentExtIdOpt.get().getUser().getId().equals(user.getId())) {
            authorizedClientRepository.deleteById(authorizedClient.getId());
            throw new StateCollisionException("External account id "
                    + authorizedClient.getExternalAccountId()
                    + " is already taken by another user. While that user is authorized, no"
                    + " other user is allowed to authorize with this dropbox account.", ErrorType.OAUTH2_EXTERNAL_ID_TAKEN);
        }
        LOGGER.debug("Persisting updated authorized client to db for user " + user.getId());
        return authorizedClientRepository.save(authorizedClient);
    }
}
