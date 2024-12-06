package com.osslot.educorder.infrastructure.user.repository;

import com.google.cloud.firestore.Firestore;
import com.osslot.educorder.domain.user.model.User.UserId;
import com.osslot.educorder.domain.user.model.UserSettings;
import com.osslot.educorder.domain.user.repository.UserSettingsRepository;
import com.osslot.educorder.infrastructure.user.repository.entity.UserSettingsEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class FirestoreUserSettingsRepository implements UserSettingsRepository {

  private final Firestore firestore;

  private static final Map<String, UserSettings> userSettingsCache = new ConcurrentHashMap<>();
  private static final List<UserSettings> googleCalendarSynchoEnabledCache = new ArrayList<>();

  public Optional<UserSettings> findByUserId(UserId userId) {
    return Optional.ofNullable(userSettingsCache.computeIfAbsent(userId.id(), this::findById));
  }

  private @Nullable UserSettings findById(String aUserId) {
    try {
      var userSettingsEntities =
          firestore
              .collection(UserSettingsEntity.PATH)
              .whereEqualTo("userId", aUserId)
              .get()
              .get()
              .toObjects(UserSettingsEntity.class);
      if (userSettingsEntities.isEmpty()) {
        return null;
      }
      return userSettingsEntities.getFirst().toDomain();
    } catch (InterruptedException | ExecutionException e) {
      log.error("Error fetching user settings for user {}", aUserId, e);
      return null;
    }
  }

  public List<UserSettings> findByGoogleCalendarSynchroEnabled(boolean synchroEnabled) {
    if (!googleCalendarSynchoEnabledCache.isEmpty()) {
      return googleCalendarSynchoEnabledCache;
    }
    try {
      var userSettingsEntities =
          firestore
              .collection(UserSettingsEntity.PATH)
              .whereEqualTo("googleCalendarSettings.synchroEnabled", synchroEnabled)
              .get()
              .get()
              .toObjects(UserSettingsEntity.class);
      googleCalendarSynchoEnabledCache.addAll(
          userSettingsEntities.stream().map(UserSettingsEntity::toDomain).toList());
    } catch (InterruptedException | ExecutionException e) {
      log.error("Error fetching user settings", e);
    }
    return googleCalendarSynchoEnabledCache;
  }

  @Override
  public void add(UserSettings userSettings) {
    var userSettingsEntity = UserSettingsEntity.fromDomain(userSettings);
    firestore.collection(UserSettingsEntity.PATH).document().set(userSettingsEntity);
  }
}
