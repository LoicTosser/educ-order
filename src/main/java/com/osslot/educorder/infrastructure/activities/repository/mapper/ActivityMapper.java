package com.osslot.educorder.infrastructure.activities.repository.mapper;

import com.google.api.services.calendar.model.Event;
import com.osslot.educorder.domain.activities.model.Activity;
import com.osslot.educorder.domain.activities.model.Location;
import com.osslot.educorder.domain.activities.model.Patient;
import com.osslot.educorder.domain.activities.repository.LocationRepository;
import com.osslot.educorder.domain.activities.repository.PatientRepository;
import com.osslot.educorder.domain.model.User.UserId;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class ActivityMapper {

  private final PatientRepository patientRepository;
  private final LocationRepository locationRepository;

  public Optional<Activity> fromEvent(UserId userId, Event event) {
    log.info(
        "Event input: {}, {}, {}, {}",
        event.getId(),
        event.getStart() == null ? "null" : event.getStart().getDateTime(),
        event.getSummary(),
        event.getLocation() != null ? event.getLocation() : "None");
    var eventId = event.getId();
    var activityType = toActivityType(event);
    var patient = toPatient(event);
    var location = toLocation(event);
    if (activityType.isEmpty() || patient.isEmpty() || location.isEmpty()) {
      return Optional.empty();
    }
    if (event.getStart() == null
        || event.getStart().getDateTime() == null
        || event.getEnd() == null
        || event.getEnd().getDateTime() == null) {
      log.warn("not considering event with missing start or end date");
      return Optional.empty();
    }
    var activityStartDate = EventDateTimeMapper.toZonedDateTime(event.getStart());
    var activityEndDate = EventDateTimeMapper.toZonedDateTime(event.getEnd());
    var duration = Duration.between(activityStartDate, activityEndDate);
    var activity =
        new Activity(
            UUID.randomUUID().toString(),
            userId,
            eventId,
            patient.get(),
            activityStartDate,
            duration,
            location.get(),
            activityType.get(),
            toActivityStatus(event));
    log.info("Activity: {}", activity);
    return Optional.of(activity);
  }

  private Optional<Patient> toPatient(Event event) {
    if (event.getSummary() == null) {
      return Optional.empty();
    }
    String eventName = event.getSummary().toLowerCase();
    return patientRepository.findAll().stream()
        .filter(
            patient ->
                eventName.contains(patient.firstName().toLowerCase())
                    || eventName.contains(patient.lastName().toLowerCase()))
        .findFirst();
  }

  private Optional<Location> toLocation(Event event) {
    String eventLocation = event.getLocation();
    if (eventLocation != null) {
      var location = locationRepository.findByAddress(eventLocation);
      if (location.isPresent()) {
        return location;
      }
      return Optional.of(new Location("", event.getLocation()));
    }
    return getDomicile();
  }

  private Optional<Location> getDomicile() {
    return locationRepository.findByName("Domicile");
  }

  private Optional<Activity.ActivityType> toActivityType(Event event) {
    if (event.getSummary() == null) {
      return Optional.empty();
    }
    String eventName = event.getSummary().toLowerCase();
    return Arrays.stream(Activity.ActivityType.values())
        .filter(activityType -> eventName.contains(activityType.getFrenchName().toLowerCase()))
        .findFirst();
  }

  private Activity.ActivityStatus toActivityStatus(Event event) {
    return switch (event.getStatus()) {
      case "cancelled" -> Activity.ActivityStatus.CANCELLED;
      case "tentative" -> Activity.ActivityStatus.TENTATIVE;
      default -> Activity.ActivityStatus.CONFIRMED;
    };
  }
}
