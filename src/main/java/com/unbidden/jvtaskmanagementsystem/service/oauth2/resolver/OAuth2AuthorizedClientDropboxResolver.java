package com.unbidden.jvtaskmanagementsystem.service.oauth2.resolver;

import com.dropbox.core.DbxApiException;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.users.FullAccount;
import com.unbidden.jvtaskmanagementsystem.dto.oauth2.OAuth2TokenResponseDto;
import com.unbidden.jvtaskmanagementsystem.exception.dropbox.GeneralDropboxException;
import com.unbidden.jvtaskmanagementsystem.exception.dropbox.SpecificDropboxException;
import com.unbidden.jvtaskmanagementsystem.exception.oauth2.OAuth2AuthorizationException;
import com.unbidden.jvtaskmanagementsystem.model.OAuth2AuthorizedClient;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.repository.ProjectRoleRepository;
import com.unbidden.jvtaskmanagementsystem.repository.oauth2.AuthorizedClientRepository;
import com.unbidden.jvtaskmanagementsystem.util.EntityUtil;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OAuth2AuthorizedClientDropboxResolver extends OAuth2AuthorizedClientAbstractResolver {
    private static final Logger LOGGER =
            LogManager.getLogger(OAuth2AuthorizedClientDropboxResolver.class);

    private DbxRequestConfig dbxRequestConfig;

    public OAuth2AuthorizedClientDropboxResolver(@Autowired EntityUtil entityUtil,
            @Autowired AuthorizedClientRepository authorizedClientRepository,
            @Autowired DbxRequestConfig dbxRequestConfig,
            @Autowired ProjectRoleRepository projectRoleRepository) {
        super(entityUtil.getClientRegistrationByName("dropbox"), authorizedClientRepository);
        this.dbxRequestConfig = dbxRequestConfig;
    }

    @Override
    public OAuth2AuthorizedClient resolveAuthorizedClient(User user,
            OAuth2TokenResponseDto tokenData,
            Optional<OAuth2AuthorizedClient> authorizedClientOpt) {
        LOGGER.info("Calling super method for user " + user.getId());
        OAuth2AuthorizedClient authorizedClient =
                super.resolveAuthorizedClient(user, tokenData, authorizedClientOpt);
        
        LOGGER.info("Setting external account id for user " + user.getId());
        authorizedClient.setExternalAccountId(
                getAccountIdFromDropbox(authorizedClient));
        Optional<OAuth2AuthorizedClient> authClientWithCurrentExtIdOpt =
                authorizedClientRepository.findByExternalAccountId(authorizedClient
                .getExternalAccountId());
        LOGGER.info("Testing whether the external id is taken.");
        if (authClientWithCurrentExtIdOpt.isPresent()
                && authClientWithCurrentExtIdOpt.get().getUser().getId() != user.getId()) {
            authorizedClientRepository.deleteById(authorizedClient.getId());
            throw new OAuth2AuthorizationException("External account id "
                    + authorizedClient.getExternalAccountId()
                    + " is already taken by another user. While that user is authorized, no"
                    + " other user is allowed to authorize with this dropbox account.");
        }
        LOGGER.info("Persisting updated authorized client to db for user " + user.getId());
        return authorizedClientRepository.save(authorizedClient);
    }

    private String getAccountIdFromDropbox(OAuth2AuthorizedClient authorizedClient) {
        DbxClientV2 dbxClient = new DbxClientV2(dbxRequestConfig, authorizedClient.getToken());
        try {
            FullAccount currentAccount = dbxClient.users().getCurrentAccount();
            LOGGER.info("Account info loaded from dropbox for user "
                    + authorizedClient.getUser().getId());
            return currentAccount.getAccountId();
        } catch (DbxApiException e) {
            throw new SpecificDropboxException("Somehow unable to aquire external account "
                    + "id for newly authorized user... Please help.", e);
        } catch (DbxException e) {
            throw new GeneralDropboxException("General dropbox exception was thrown.", e);
        }
    }
}
