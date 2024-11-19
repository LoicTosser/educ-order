package com.osslot.educorder.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EducOrderApplication {

  public static final String APPLICATION_NAME = "EducOrder";

  public static void main(String[] args) {
    SpringApplication.run(EducOrderApplication.class, args);
  }
}
