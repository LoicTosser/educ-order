package com.osslot.educorder.infrastructure.activities.repository;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.osslot.educorder.domain.user.model.User.UserId;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class GoogleCredentials {
  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  public static final String CLIENT_REGISTRATION_ID = "google";

  private final OAuth2AuthorizedClientManager authorizedClientManager;

  public Optional<Credential> getCredentials(UserId userId) {
    final NetHttpTransport httpTransport;
    try {
      httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    } catch (GeneralSecurityException | IOException e) {
      log.error("Error while creating http transport", e);
      return Optional.empty();
    }

    OAuth2AuthorizeRequest authorizeRequest =
        OAuth2AuthorizeRequest.withClientRegistrationId(CLIENT_REGISTRATION_ID)
            .principal(userId.id())
            .build();

    OAuth2AuthorizedClient authorizedClient =
        this.authorizedClientManager.authorize(authorizeRequest);

    Credential.AccessMethod accessMethod = BearerToken.authorizationHeaderAccessMethod();
    Credential credential =
        new Credential.Builder(accessMethod)
            .setTransport(httpTransport)
            .setJsonFactory(JSON_FACTORY)
            .setTokenServerEncodedUrl("https://oauth2.googleapis.com/token")
            .build()
            .setAccessToken(authorizedClient.getAccessToken().getTokenValue())
            .setExpirationTimeMilliseconds(
                authorizedClient.getAccessToken().getExpiresAt().toEpochMilli());

    return Optional.of(credential);
  }
}
