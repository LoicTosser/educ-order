package com.osslot.educorder.domain.model;

public record ActivityKilometers(
    Activity activity, Location from, Location to, Long distanceFrom, Long distanceTo) {}
