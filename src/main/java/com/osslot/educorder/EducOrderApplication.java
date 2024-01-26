package com.osslot.educorder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EducOrderApplication {

  public static final String APPLICATION_NAME = "EducOrder";

  public static void main(String[] args) {
    SpringApplication.run(EducOrderApplication.class, args);
  }
}
