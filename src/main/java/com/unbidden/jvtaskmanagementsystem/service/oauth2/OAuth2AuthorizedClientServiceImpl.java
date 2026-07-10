package com.unbidden.jvtaskmanagementsystem.service.oauth2;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unbidden.jvtaskmanagementsystem.dto.oauth2.OAuth2TokenResponseDto;
import com.unbidden.jvtaskmanagementsystem.exception.EntityNotFoundException;
import com.unbidden.jvtaskmanagementsystem.exception.ErrorType;
import com.unbidden.jvtaskmanagementsystem.model.ClientRegistration;
import com.unbidden.jvtaskmanagementsystem.model.OAuth2AuthorizedClient;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.repository.oauth2.AuthorizedClientRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuth2AuthorizedClientServiceImpl implements OAuth2AuthorizedClientService {
    private final AuthorizedClientRepository authorizedClientRepository;

    @NonNull
    @Override
    @Transactional
    public OAuth2AuthorizedClient register(@NonNull User user,
            @NonNull OAuth2TokenResponseDto tokenData,
            @NonNull ClientRegistration clientRegistration,
            @NonNull String externalId) {
        final Optional<OAuth2AuthorizedClient> clientOpt = authorizedClientRepository
                .findByUserIdAndRegistrationName(user.getId(),
                clientRegistration.getClientName());
        final OAuth2AuthorizedClient client = clientOpt.orElse(new OAuth2AuthorizedClient());

        client.setClientRegistrationName(clientRegistration.getClientName());
        client.setUser(user);
        client.setAquiredAt(LocalDateTime.now());
        client.setExpiresIn(tokenData.getExpiresIn());
        client.setRefreshToken(tokenData.getRefreshToken());
        client.setExternalAccountId(externalId);
        client.setToken(tokenData.getAccessToken());
        return authorizedClientRepository.save(client);
    }

    @NonNull
    @Override
    @Transactional(readOnly = true)
    public Optional<OAuth2AuthorizedClient> findClient(@NonNull User user,
            @NonNull ClientRegistration registration) {
        return authorizedClientRepository.findByUserIdAndRegistrationName(user.getId(),
                registration.getClientName());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isExternalIdTaken(@NonNull String externalId) {
        return authorizedClientRepository.existsByExternalAccountId(externalId);
    }

    @NonNull
    @Override
    @Transactional
    public OAuth2AuthorizedClient update(@NonNull Long clientId, OAuth2TokenResponseDto tokenData) {
        final OAuth2AuthorizedClient client = authorizedClientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Authorized client with ID "
                + clientId + " does not exist.", ErrorType.OAUTH2_INTERNAL_FAILURE));

        client.setAquiredAt(LocalDateTime.now());
        client.setExpiresIn(tokenData.getExpiresIn());
        client.setRefreshToken(tokenData.getRefreshToken());
        client.setToken(tokenData.getAccessToken());

        return client;
    }

    @Override
    @Transactional
    public void delete(@NonNull Long id) {
        authorizedClientRepository.deleteById(id);
    }
}
