package com.osslot.educorder.infrastructure.security.oauth2.repository.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuth2AuthorizedClientEntity {

  public static final String PATH = "oauth2_authorized_clients";

  private String clientRegistrationId;
  private String principalName;
  private String accessTokenType;
  private String accessTokenValue;
  private Instant accessTokenIssuedAt;
  private Instant accessTokenExpiresAt;
  private List<String> accessTokenScopes;
  private String refreshTokenValue;
  private Instant refreshTokenIssuedAt;

  public static OAuth2AuthorizedClientEntity fromDomain(
      OAuth2AuthorizedClient oAuth2AuthorizedClient) {
    var refreshToken = oAuth2AuthorizedClient.getRefreshToken();
    var accessToken = oAuth2AuthorizedClient.getAccessToken();
    return OAuth2AuthorizedClientEntity.builder()
        .clientRegistrationId(oAuth2AuthorizedClient.getClientRegistration().getRegistrationId())
        .principalName(oAuth2AuthorizedClient.getPrincipalName())
        .accessTokenType(accessToken.getTokenType().getValue())
        .accessTokenValue(accessToken.getTokenValue())
        .accessTokenExpiresAt(accessToken.getExpiresAt())
        .accessTokenIssuedAt(accessToken.getIssuedAt())
        .accessTokenScopes(new ArrayList<>(accessToken.getScopes()))
        .refreshTokenValue(refreshToken == null ? null : refreshToken.getTokenValue())
        .refreshTokenIssuedAt(refreshToken == null ? null : refreshToken.getIssuedAt())
        .build();
  }
}
