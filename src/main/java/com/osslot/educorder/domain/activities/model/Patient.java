package com.osslot.educorder.domain.activities.model;

public record Patient(
    String firstName, String lastName, String fullName, Institution institution) {}
