package com.osslot.educorder.domain.activities.repository;

import com.osslot.educorder.domain.activities.model.Activity;
import com.osslot.educorder.domain.user.model.User.UserId;
import java.time.ZonedDateTime;
import java.util.List;

public interface ActivityRepository {

  List<Activity> findAllBetween(UserId userId, ZonedDateTime start, ZonedDateTime end);

  List<Activity> add(List<Activity> activities);

  default List<Activity> synchronyze(List<Activity> activities) {
    var activitiesToDelete = activities.stream().filter(Activity::isCancelled).toList();
    deleteActivities(activitiesToDelete);
    var activitiesToUpsert =
        activities.stream().filter(activity -> !activity.isCancelled()).toList();
    upsertActivities(activitiesToUpsert);
    return activities;
  }

  void upsertActivities(List<Activity> activitiesToUpsert);

  void deleteActivities(List<Activity> activitiesToDelete);
}
