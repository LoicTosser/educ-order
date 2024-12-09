package com.osslot.educorder.infrastructure.security.oauth2.repository.mapper;

import com.osslot.educorder.infrastructure.security.oauth2.repository.entity.OAuth2AuthorizedClientEntity;
import java.util.HashSet;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OAuth2AuthorizedClientMapper {

  private final ClientRegistrationRepository clientRegistrationRepository;

  public OAuth2AuthorizedClient fromEntity(OAuth2AuthorizedClientEntity entity) {
    String clientRegistrationId = entity.getClientRegistrationId();
    ClientRegistration clientRegistration =
        this.clientRegistrationRepository.findByRegistrationId(clientRegistrationId);
    if (clientRegistration == null) {
      throw new RuntimeException(
          "The ClientRegistration with id '"
              + clientRegistrationId
              + "' exists in the data source, "
              + "however, it was not found in the ClientRegistrationRepository.");
    }
    return new OAuth2AuthorizedClient(
        clientRegistration,
        entity.getPrincipalName(),
        toAccessToken(entity),
        toRefreshToken(entity));
  }

  @NotNull
  private static OAuth2AccessToken toAccessToken(OAuth2AuthorizedClientEntity entity) {
    if (!OAuth2AccessToken.TokenType.BEARER
        .getValue()
        .equalsIgnoreCase(entity.getAccessTokenType())) {
      throw new IllegalArgumentException(
          "Unsupported access token type: " + entity.getAccessTokenType());
    }
    return new OAuth2AccessToken(
        OAuth2AccessToken.TokenType.BEARER,
        entity.getAccessTokenValue(),
        entity.getAccessTokenIssuedAt(),
        entity.getAccessTokenExpiresAt(),
        new HashSet<>(entity.getAccessTokenScopes()));
  }

  @Nullable
  private static OAuth2RefreshToken toRefreshToken(OAuth2AuthorizedClientEntity entity) {
    var refreshTokenValue = entity.getRefreshTokenValue();
    if (refreshTokenValue == null) {
      return null;
    }
    var refreshTokenIssuedAt =
        entity.getRefreshTokenIssuedAt() == null
            ? entity.getAccessTokenIssuedAt()
            : entity.getRefreshTokenIssuedAt();
    return new OAuth2RefreshToken(refreshTokenValue, refreshTokenIssuedAt);
  }
}
