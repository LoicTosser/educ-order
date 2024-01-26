package com.osslot.educorder.domain.repository;

import com.osslot.educorder.domain.model.Activity;
import java.util.List;

public interface ActivityRepository {

  List<Activity> findAllByMonth(int year, int month);

  List<Activity> add(List<Activity> activities);
}
