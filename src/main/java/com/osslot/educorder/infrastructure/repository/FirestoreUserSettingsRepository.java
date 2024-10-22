package com.osslot.educorder.infrastructure.repository;

import com.google.cloud.firestore.Firestore;
import com.osslot.educorder.domain.model.UserSettings;
import com.osslot.educorder.domain.repository.UserSettingsRepository;
import com.osslot.educorder.infrastructure.repository.entity.UserSettingsEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import javax.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class FirestoreUserSettingsRepository implements UserSettingsRepository {

  private final Firestore firestore;

  public static final String CALENDAR_ID =
      "b32341848b6870ac8899d82601c990e3146d29a36cc404a6df2bfc6aa893c9ae@group.calendar.google.com";

  private static final Map<String, UserSettings> userSettingsCache =
      new ConcurrentHashMap<>();
  private static final List<UserSettings> googleCalendarSynchoEnabledCache = new ArrayList<>();

  @PostConstruct
  public void init() {
    log.info("FirestoreUserSettingsRepository initialized");
    firestore
        .collection(UserSettingsEntity.PATH)
        .document()
        .set(
            new UserSettingsEntity(
                new UserSettingsEntity.UserEntity("1234", "test", "test@gmail.com"),
                new UserSettingsEntity.GoogleCalendarSettingsEntity(CALENDAR_ID, false)));
  }

  public Optional<UserSettings> findByUserId(String userId) {
    return Optional.ofNullable(userSettingsCache.computeIfAbsent(
        userId,
        aUserId -> {
          try {
            var userSettingsEntities =
                firestore
                    .collection(UserSettingsEntity.PATH)
                    .whereEqualTo("user.id", userId)
                    .get()
                    .get()
                    .toObjects(UserSettingsEntity.class);
            if (userSettingsEntities.isEmpty()) {
              return null;
            }
            return userSettingsEntities.getFirst().toDomain();
          } catch (InterruptedException | ExecutionException e) {
            log.error("Error fetching user settings", e);
            return null;
          }
        }));
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
