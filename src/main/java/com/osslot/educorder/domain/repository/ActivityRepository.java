package com.osslot.educorder.domain.repository;

import com.osslot.educorder.domain.model.Activity;
import com.osslot.educorder.domain.model.UserSettings.User;
import java.time.ZonedDateTime;
import java.util.List;

public interface ActivityRepository {

  List<Activity> findAllByMonth(int year, int month);

  List<Activity> findAllBetween(User user, ZonedDateTime start, ZonedDateTime end);

  List<Activity> add(List<Activity> activities);

  List<Activity> synchronyze(List<Activity> activities);
}
