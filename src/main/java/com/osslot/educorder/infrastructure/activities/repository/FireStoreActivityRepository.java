package com.osslot.educorder.infrastructure.activities.repository;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.osslot.educorder.domain.activities.model.Activity;
import com.osslot.educorder.domain.activities.repository.ActivityRepository;
import com.osslot.educorder.domain.user.model.User.UserId;
import com.osslot.educorder.infrastructure.activities.repository.entity.ActivityEntity;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class FireStoreActivityRepository implements ActivityRepository {

  private final Firestore firestore;

  @Override
  public List<Activity> findAllBetween(UserId userId, ZonedDateTime start, ZonedDateTime end) {
    var startUTC = start.withZoneSameInstant(ZoneOffset.UTC);
    var endUTC = end.withZoneSameInstant(ZoneOffset.UTC);
    try {
      var querySnapshot =
          firestore
              .collection(ActivityEntity.PATH)
              .whereEqualTo("userId", userId.id())
              .whereGreaterThanOrEqualTo("beginDate", startUTC.toInstant())
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
  public void upsertActivities(List<Activity> activitiesToUpsert) {
    var existingQuerySnapshotDocuments = getActivityDocuments(activitiesToUpsert);
    var activitiesToUpdateByDocumentReference =
        getActivitiesByDocumentReference(activitiesToUpsert, existingQuerySnapshotDocuments);
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

  @Override
  public void deleteActivities(List<Activity> activities) {
    var activityDocuments = getActivityDocuments(activities);

    activityDocuments.forEach(
        document -> {
          try {
            document.getReference().delete().get();
          } catch (InterruptedException | ExecutionException e) {
            log.error("Error deleting activity", e);
          }
        });
  }

  private List<QueryDocumentSnapshot> getActivityDocuments(List<Activity> activities) {
    if (activities.isEmpty()) {
      return List.of();
    }
    AtomicInteger counter = new AtomicInteger();
    int groupSize = 10;
    Map<Integer, List<Activity>> mapOfChunks =
        activities.stream()
            .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / groupSize));
    // Create a list containing the lists of chunks
    List<List<Activity>> activitiesChunks = new ArrayList<>(mapOfChunks.values());
    return activitiesChunks.stream()
        .flatMap(
            activitiesChunk -> {
              var activityEventIds = activitiesChunk.stream().map(Activity::eventId).toList();
              var activitiesCollection = firestore.collection(ActivityEntity.PATH);
              Query query = activitiesCollection.whereIn("eventId", activityEventIds);
              try {
                return query.get().get().getDocuments().stream();
              } catch (InterruptedException | ExecutionException e) {
                log.error("Error fetching activities", e);
              }
              return Stream.empty();
            })
        .toList();
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
