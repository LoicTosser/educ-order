package com.osslot.educorder.infrastructure.activities.repository;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.auth.oauth2.AccessToken;
import com.osslot.educorder.domain.user.model.User.UserId;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class GoogleCredentials {
  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  public static final String CLIENT_REGISTRATION_ID = "google";

  private final OAuth2AuthorizedClientService authorizedClientService;

  public Optional<Credential> getCredentials(UserId userId) {
    final NetHttpTransport httpTransport;
    try {
      httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    } catch (GeneralSecurityException | IOException e) {
      log.error("Error while creating http transport", e);
      return Optional.empty();
    }
    OAuth2AuthorizedClient client =
        authorizedClientService.loadAuthorizedClient(CLIENT_REGISTRATION_ID, userId.id());
    OAuth2AccessToken accessToken = client.getAccessToken();

    com.google.auth.oauth2.GoogleCredentials googleCredentials =
        com.google.auth.oauth2.GoogleCredentials.create(
            new AccessToken(accessToken.getTokenValue(), Date.from(accessToken.getExpiresAt())));

    Credential.AccessMethod accessMethod = BearerToken.authorizationHeaderAccessMethod();
    Credential credential =
        new Credential.Builder(accessMethod)
            .setTransport(httpTransport)
            .setJsonFactory(JSON_FACTORY)
            .setTokenServerEncodedUrl("https://oauth2.googleapis.com/token")
            .build()
            .setAccessToken(googleCredentials.getAccessToken().getTokenValue());

    // Check if the access token is expired and refresh it if necessary
    if (credential.getExpiresInSeconds() != null && credential.getExpiresInSeconds() <= 60) {
      try {
        if (!credential.refreshToken()) {
          log.warn("Failed to refresh token");
          return Optional.empty();
        }
      } catch (IOException e) {
        log.error("Error while refreshing token", e);
        return Optional.empty();
      }
    }

    return Optional.of(credential);
  }
}
