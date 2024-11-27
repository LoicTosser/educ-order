package com.osslot.educorder.domain.activities.model;

import java.util.Arrays;

public enum Institution {
  APAJH("APHJH"),
  ADIAPH("ADIAPH"),
  LIBERAL("Libéral");

  private final String frenchName;

  Institution(String frenchName) {
    this.frenchName = frenchName;
  }

  public String getFrenchName() {
    return frenchName;
  }

  public static Institution fromFrenchName(String frenchName) {
    return Arrays.stream(Institution.values())
        .filter(institution -> institution.getFrenchName().equals(frenchName))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("No institution matches " + frenchName));
  }
}
