package com.osslot.educorder.domain.repository;

import com.osslot.educorder.domain.model.Activity;
import java.time.ZonedDateTime;
import java.util.List;

public interface CalendarRepository {

  List<Activity> fromCalendar(int year, int month);

  List<Activity> fromCalendar(ZonedDateTime start, ZonedDateTime end);
}
