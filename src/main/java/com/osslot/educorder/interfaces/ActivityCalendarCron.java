package com.osslot.educorder.interfaces;

import com.osslot.educorder.application.ImportActivitiesFromCalendar;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ActivityCalendarCron {

  private final ImportActivitiesFromCalendar importActivitiesFromCalendar;

  public ActivityCalendarCron(ImportActivitiesFromCalendar importActivitiesFromCalendar) {
    this.importActivitiesFromCalendar = importActivitiesFromCalendar;
  }

  @Scheduled(cron = "0 */1 * * * *")
  public void syncActivities() {
    log.info("[CRON} Syncing activities from calendar");
    importActivitiesFromCalendar.syncActivities();
    ;
  }
}
