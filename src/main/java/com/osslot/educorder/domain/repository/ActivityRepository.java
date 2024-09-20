package com.osslot.educorder.domain.repository;

import com.osslot.educorder.domain.model.Activity;
import java.time.ZonedDateTime;
import java.util.List;

public interface ActivityRepository {

  List<Activity> findAllByMonth(int year, int month);

  List<Activity> findAllBetween(ZonedDateTime start, ZonedDateTime end);

  List<Activity> add(List<Activity> activities);
}
