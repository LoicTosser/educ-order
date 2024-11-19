package com.osslot.educorder.domain.activities.repository;

import com.osslot.educorder.domain.activities.model.Activity;
import com.osslot.educorder.domain.model.User.UserId;
import java.time.ZonedDateTime;
import java.util.List;

public interface ActivityRepository {

  List<Activity> findAllByMonth(int year, int month);

  List<Activity> findAllBetween(UserId userId, ZonedDateTime start, ZonedDateTime end);

  List<Activity> add(List<Activity> activities);

  List<Activity> synchronyze(List<Activity> activities);
}
