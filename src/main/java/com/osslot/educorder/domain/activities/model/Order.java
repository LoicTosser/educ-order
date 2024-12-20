package com.osslot.educorder.domain.activities.model;

import com.osslot.educorder.domain.patient.model.Patient;
import java.util.List;

public class Order {
  private String id;
  private Patient patient;
  private List<ArticleLine> articleLines;

  public record ArticleLine(Article article, double quantity) {}

  public record Customer(String id, String name) {}
}
