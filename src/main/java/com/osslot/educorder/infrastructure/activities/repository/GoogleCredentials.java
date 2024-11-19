package com.osslot.educorder.infrastructure.activities.repository;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.docs.v1.DocsScopes;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.oauth2.AccessToken;
import com.osslot.educorder.domain.model.User.UserId;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.List;
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
  private static final String TOKENS_DIRECTORY_PATH = "tokens";

  private final OAuth2AuthorizedClientService authorizedClientService;

  /**
   * Global instance of the scopes required by this quickstart. If modifying these scopes, delete
   * your previously saved tokens/ folder.
   */
  private static final List<String> SCOPES =
      List.of(
          SheetsScopes.SPREADSHEETS,
          CalendarScopes.CALENDAR_EVENTS,
          DriveScopes.DRIVE,
          DocsScopes.DOCUMENTS);

  private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

  /**
   * Creates an authorized Credential object.
   *
   * @param HTTP_TRANSPORT The network HTTP Transport.
   * @return An authorized Credential object.
   * @throws java.io.IOException If the credentials.json file cannot be found.
   */
  public Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
    // Load client secrets.
    var in = this.getClass().getResourceAsStream(CREDENTIALS_FILE_PATH);
    if (in == null) {
      throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
    }
    var clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

    // Build flow and trigger user authorization request.
    var flow =
        new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
            .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline")
            .build();
    var receiver = new LocalServerReceiver.Builder().setPort(8888).build();
    return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
  }

  public Optional<Credential> getCredentials(UserId userId) {
    final NetHttpTransport httpTransport;
    try {
      httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    } catch (GeneralSecurityException | IOException e) {
      log.error("Error while creating http transport", e);
      return Optional.empty();
    }
    OAuth2AuthorizedClient client =
        authorizedClientService.loadAuthorizedClient("google", userId.id());
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
