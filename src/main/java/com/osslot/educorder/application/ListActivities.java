package com.osslot.educorder.application;

import com.osslot.educorder.domain.model.Activity;
import com.osslot.educorder.domain.repository.ActivityRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class ListActivities {

    private final ActivityRepository activityRepository;

    public List<Activity> listActivities(ZonedDateTime start, ZonedDateTime end) {
        return activityRepository.findAllBetween(start, end);
    }

}
