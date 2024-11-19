package com.osslot.educorder.infrastructure.activities.repository.abby;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Builder
@NoArgsConstructor
@Setter
public class Product {
  private String _id;
  private String companyId;
  private int productType;
  private int productUnit;
  private String designation;
  private String description;
  private double unitPrice;
  private String reference;
  private int vatPercentage;
  private int vatId;
  private String codeNature;
  private String tpUnit;
  private boolean isDeliveryOfGood;
  private int __v;
  private boolean tiersPrestationIsActivatedForThisProduct;
  private int quantity;
  private boolean hasDescription;

  // getters and setters
}
