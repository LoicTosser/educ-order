package com.osslot.educorder.infrastructure.repository;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.osslot.educorder.domain.model.Activity;
import com.osslot.educorder.domain.model.UserSettings.User;
import com.osslot.educorder.domain.repository.ActivityRepository;
import com.osslot.educorder.infrastructure.repository.entity.ActivityEntity;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class FireStoreActivityRepository implements ActivityRepository {

  private final Firestore firestore;

  @Override
  public List<Activity> findAllByMonth(int year, int month) {
    return List.of();
  }

  @Override
  public List<Activity> findAllBetween(User user, ZonedDateTime start, ZonedDateTime end) {
    var startUTC = start.withZoneSameInstant(ZoneOffset.UTC);
    var endUTC = end.withZoneSameInstant(ZoneOffset.UTC);
    try {
      var querySnapshot =
          firestore
              .collection(ActivityEntity.PATH)
              .whereEqualTo("userEntity.id", user.id())
              .whereGreaterThanOrEqualTo(
                  "beginDate",
                  startUTC.toInstant())
              .whereLessThanOrEqualTo("endDate", endUTC.toInstant())
              .get()
              .get();
      return querySnapshot.getDocuments().stream()
          .map(queryDocumentSnapshot -> queryDocumentSnapshot.toObject(ActivityEntity.class))
          .map(ActivityEntity::toDomain)
          .toList();
    } catch (InterruptedException | ExecutionException e) {
      log.error("Error fetching activities", e);
    }
    return List.of();
  }

  @Override
  public List<Activity> add(List<Activity> activities) {
    activities.forEach(this::upsert);
    return activities;
  }

  @Override
  public List<Activity> synchronyze(List<Activity> activities) {
    var activitiesToDelete = activities.stream().filter(Activity::isCancelled).toList();
    deleteActivities(activitiesToDelete);
    var activitiesToUpsert =
        activities.stream().filter(activity -> !activity.isCancelled()).toList();
    upsertActivities(activitiesToUpsert);
    return activities;
  }

  private void upsertActivities(List<Activity> activitiesToUpsert) {
    var existingQuerySnapshotDocuments = getActivityDocuments(activitiesToUpsert);
    var activitiesToUpdateByDocumentReference =
        getActivitiesByDocumentReference(
            activitiesToUpsert, existingQuerySnapshotDocuments.orElse(List.of()));
    updateActivities(activitiesToUpdateByDocumentReference);
    var activitiesToUpdate = activitiesToUpdateByDocumentReference.values().stream().toList();
    var activitiesToInsert =
        activitiesToUpsert.stream()
            .filter(activity -> !activitiesToUpdate.contains(activity))
            .toList();
    activitiesToInsert.forEach(this::upsert);
  }

  private static void updateActivities(
      Map<DocumentReference, Activity> activitiesToUpdateByDocumentReference) {
    activitiesToUpdateByDocumentReference.forEach(
        (documentReference, activity) -> {
          var activityEntity = ActivityEntity.fromDomain(activity);
          try {
            documentReference.set(activityEntity).get();
          } catch (InterruptedException | ExecutionException e) {
            log.error("Error updating activity", e);
          }
        });
  }

  private void upsert(Activity activity) {
    var activityEntity = ActivityEntity.fromDomain(activity);
    var document = firestore.collection(ActivityEntity.PATH).document(activityEntity.getId());
    try {
      document.set(activityEntity).get();
    } catch (InterruptedException | ExecutionException e) {
      log.error("Error adding activity", e);
    }
  }

  private void deleteActivities(List<Activity> activities) {
    var activityDocuments = getActivityDocuments(activities);
    activityDocuments.ifPresent(
        documents ->
            documents.forEach(
                document -> {
                  try {
                    document.getReference().delete().get();
                  } catch (InterruptedException | ExecutionException e) {
                    log.error("Error deleting activity", e);
                  }
                }));
  }

  private Optional<List<QueryDocumentSnapshot>> getActivityDocuments(List<Activity> activities) {
    if (activities.isEmpty()) {
      return Optional.empty();
    }
    var activityEventIds = activities.stream().map(Activity::eventId).toList();
    var activitiesCollection = firestore.collection(ActivityEntity.PATH);
    Query query = activitiesCollection.whereIn("eventId", activityEventIds);
    List<QueryDocumentSnapshot> documents = null;
    try {
      documents = query.get().get().getDocuments();
    } catch (InterruptedException | ExecutionException e) {
      log.error("Error fetching activities", e);
    }
    return Optional.ofNullable(documents);
  }

  private Map<DocumentReference, Activity> getActivitiesByDocumentReference(
      List<Activity> activities, List<QueryDocumentSnapshot> existingQueryDocumentSnapshots) {
    return existingQueryDocumentSnapshots.stream()
        .collect(
            Collectors.toMap(
                DocumentSnapshot::getReference,
                queryDocumentSnapshot -> {
                  var activityEntity = queryDocumentSnapshot.toObject(ActivityEntity.class);
                  return activities.stream()
                      .filter(activity -> activity.eventId().equals(activityEntity.getEventId()))
                      .findFirst()
                      .orElseThrow();
                }));
  }
}
