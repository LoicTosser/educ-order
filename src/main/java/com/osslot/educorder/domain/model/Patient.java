package com.osslot.educorder.domain.model;

public record Patient(
    String firstName, String lastName, String fullName, Institution institution) {}
