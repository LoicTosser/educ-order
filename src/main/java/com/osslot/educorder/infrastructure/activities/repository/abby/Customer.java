package com.osslot.educorder.infrastructure.activities.repository.abby;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Builder
@Getter
@NoArgsConstructor
@Setter
public class Customer {
  private String id;
  private String firstname;
  private String lastname;
  private String email;
  private String address;
  private String city;
  private String zipCode;
  private String country;
  private Company company;
  private String token;
  private boolean tiersPrestationIsActivated;
  private Company customerCompany;

  // getters and setters
}
