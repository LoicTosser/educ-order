package com.osslot.educorder.infrastructure.repository;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.osslot.educorder.domain.model.ActivitySyncToken;
import com.osslot.educorder.domain.model.UserSettings.User;
import com.osslot.educorder.domain.repository.ActivitySyncTokenRepository;
import com.osslot.educorder.infrastructure.repository.entity.ActivitySyncTokenEntity;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class FirestoreActivitySyncTokenRepository implements ActivitySyncTokenRepository {

  private final Firestore firestore;
  private static final Map<String, ActivitySyncToken> activitySyncTokenCache =
      new ConcurrentHashMap<>();

  @Override
  public Optional<ActivitySyncToken> getCurrentActivitySyncToken(User user) {
    return Optional.ofNullable(
        activitySyncTokenCache.computeIfAbsent(
            user.id(),
            userId -> {
              try {
                var activitySyncTokenEntities =
                    findByUserId(user).toObjects(ActivitySyncTokenEntity.class);
                if (activitySyncTokenEntities.isEmpty()) {
                  return null;
                }
                return activitySyncTokenEntities.getFirst().toDomain();
              } catch (InterruptedException | ExecutionException e) {
                log.error("Error fetching activity sync token", e);
                return null;
              }
            }));
  }

  @Override
  public void setCurrentActivitySyncToken(User user, ActivitySyncToken activitySyncToken) {
    var activitySyncTokenEntity = ActivitySyncTokenEntity.fromDomain(activitySyncToken);
    try {
      var activitySyncTokenEntities = findByUserId(user);
      if (activitySyncTokenEntities.isEmpty()) {
        firestore.collection(ActivitySyncTokenEntity.PATH).document().set(activitySyncTokenEntity);
      } else {
        activitySyncTokenEntities
            .getDocuments()
            .getFirst()
            .getReference()
            .set(activitySyncTokenEntity);
      }
    } catch (InterruptedException | ExecutionException e) {
      log.error("Error setting activity sync token", e);
    }
    activitySyncTokenCache.put(user.id(), activitySyncToken);
  }

  private QuerySnapshot findByUserId(User user) throws InterruptedException, ExecutionException {
    return firestore
        .collection(ActivitySyncTokenEntity.PATH)
        .whereEqualTo("userId", user.id())
        .get()
        .get();
  }
}
