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
public class PaymentMethodUsed {
  private Integer value;
  private String other;
  private Integer priceTotalTax;
}
