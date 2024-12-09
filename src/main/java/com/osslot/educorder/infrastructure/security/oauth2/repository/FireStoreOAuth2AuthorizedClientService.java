package com.osslot.educorder.infrastructure.security.oauth2.repository;

import com.google.cloud.firestore.Firestore;
import com.osslot.educorder.infrastructure.security.oauth2.repository.entity.OAuth2AuthorizedClientEntity;
import com.osslot.educorder.infrastructure.security.oauth2.repository.mapper.OAuth2AuthorizedClientMapper;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class FireStoreOAuth2AuthorizedClientService implements OAuth2AuthorizedClientService {

  private final Firestore firestore;
  private final OAuth2AuthorizedClientMapper oAuth2AuthorizedClientMapper;

  @Override
  public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(
      String clientRegistrationId, String principalName) {
    Objects.requireNonNull(clientRegistrationId, "clientRegistrationId cannot be null");
    Objects.requireNonNull(principalName, "principalName cannot be null");
    try {
      List<OAuth2AuthorizedClientEntity> oAuth2AuthorizedClientEntities =
          firestore
              .collection(OAuth2AuthorizedClientEntity.PATH)
              .whereEqualTo("clientRegistrationId", clientRegistrationId)
              .whereEqualTo("principalName", principalName)
              .get()
              .get()
              .toObjects(OAuth2AuthorizedClientEntity.class);
      if (oAuth2AuthorizedClientEntities.isEmpty()) {
        return null;
      }
      var entity = oAuth2AuthorizedClientEntities.getFirst();
      return (T) oAuth2AuthorizedClientMapper.fromEntity(entity);
    } catch (InterruptedException | ExecutionException e) {
      log.error("Error fetching OAuth2AuthorizedClient", e);
      return null;
    }
  }

  @Override
  public void saveAuthorizedClient(
      OAuth2AuthorizedClient authorizedClient, Authentication principal) {
    Objects.requireNonNull(authorizedClient, "authorizedClient cannot be null");
    Objects.requireNonNull(principal, "principal cannot be null");
    var entity = OAuth2AuthorizedClientEntity.fromDomain(authorizedClient);
    try {
      var existingDocuments =
          firestore
              .collection(OAuth2AuthorizedClientEntity.PATH)
              .whereEqualTo("clientRegistrationId", entity.getClientRegistrationId())
              .whereEqualTo("principalName", entity.getPrincipalName())
              .get()
              .get();
      if (existingDocuments.isEmpty()) {
        firestore.collection(OAuth2AuthorizedClientEntity.PATH).add(entity).get();
      } else {
        firestore
            .collection(OAuth2AuthorizedClientEntity.PATH)
            .document(existingDocuments.getDocuments().getFirst().getId())
            .set(entity)
            .get();
      }
    } catch (InterruptedException | ExecutionException e) {
      log.error("Error fetching OAuth2AuthorizedClient", e);
    }
  }

  @Override
  public void removeAuthorizedClient(String clientRegistrationId, String principalName) {
    Objects.requireNonNull(clientRegistrationId, "clientRegistrationId cannot be null");
    Objects.requireNonNull(principalName, "principalName cannot be null");
    try {
      var existingDocuments =
          firestore
              .collection(OAuth2AuthorizedClientEntity.PATH)
              .whereEqualTo("clientRegistrationId", clientRegistrationId)
              .whereEqualTo("principalName", principalName)
              .get()
              .get();
      if (!existingDocuments.isEmpty()) {
        firestore
            .collection(OAuth2AuthorizedClientEntity.PATH)
            .document(existingDocuments.getDocuments().getFirst().getId())
            .delete()
            .get();
      }
    } catch (InterruptedException | ExecutionException e) {
      log.error("Error fetching OAuth2AuthorizedClient", e);
    }
  }
}
