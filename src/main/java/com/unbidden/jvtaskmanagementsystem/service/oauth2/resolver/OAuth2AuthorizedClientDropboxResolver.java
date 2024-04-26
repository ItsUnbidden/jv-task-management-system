package com.unbidden.jvtaskmanagementsystem.service.oauth2.resolver;

import com.dropbox.core.DbxApiException;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.users.FullAccount;
import com.unbidden.jvtaskmanagementsystem.dto.internal.OAuth2TokenResponseDto;
import com.unbidden.jvtaskmanagementsystem.exception.OAuth2AuthorizationException;
import com.unbidden.jvtaskmanagementsystem.exception.ThirdPartyApiException;
import com.unbidden.jvtaskmanagementsystem.model.OAuth2AuthorizedClient;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.repository.ProjectRoleRepository;
import com.unbidden.jvtaskmanagementsystem.repository.oauth2.AuthorizedClientRepository;
import com.unbidden.jvtaskmanagementsystem.service.util.EntityUtil;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OAuth2AuthorizedClientDropboxResolver extends OAuth2AuthorizedClientAbstractResolver {
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
        OAuth2AuthorizedClient setUpAuthorizedClient =
                super.resolveAuthorizedClient(user, tokenData, authorizedClientOpt);
        setUpAuthorizedClient.setExternalAccountId(
                getAccountIdFromDropbox(setUpAuthorizedClient));
        Optional<OAuth2AuthorizedClient> authClientWithCurrentExtIdOpt =
                authorizedClientRepository.findByExternalAccountId(setUpAuthorizedClient
                .getExternalAccountId());
        if (authClientWithCurrentExtIdOpt.isPresent()
                && authClientWithCurrentExtIdOpt.get().getUser().getId() != user.getId()) {
            authorizedClientRepository.deleteById(setUpAuthorizedClient.getId());
            throw new OAuth2AuthorizationException("External account id "
                    + setUpAuthorizedClient.getExternalAccountId()
                    + " is already taken by another user. While that user is authorized, no"
                    + " other user is allowed to authorize with this dropbox account.");
        }
        return authorizedClientRepository.save(setUpAuthorizedClient);
    }

    private String getAccountIdFromDropbox(OAuth2AuthorizedClient authorizedClient) {
        DbxClientV2 dbxClient = new DbxClientV2(dbxRequestConfig, authorizedClient.getToken());
        try {
            FullAccount currentAccount = dbxClient.users().getCurrentAccount();
            return currentAccount.getAccountId();
        } catch (DbxApiException e) {
            throw new ThirdPartyApiException("Somehow unable to aquire external account "
                    + "id for newly authorized user... Please help.", e);
        } catch (DbxException e) {
            throw new ThirdPartyApiException("General dropbox exception was thrown.", e);
        }
    }
}
