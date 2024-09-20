package com.osslot.educorder.infrastructure.repository.abby;

import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
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
public class Invoice {
  private String id;
  private String number;
  private String title;
  private String type;
  private String state;
  private long emittedAt;
  private long dueAt;
  private Customer customer;
  private Opportunity opportunity;
  private Instant paidAt;
  private Long remainingAmountWithoutTax;
  private Long remainingAmountWithTax;
  private Long totalAmountWithoutTaxAfterDiscount;
  private Long totalAmountWithTaxAfterDiscount;
  private Long cancelledAmountWithoutTax;
  private Long cancelledAmountWithTax;
  private boolean finalizable;
  private boolean isOnlinePaymentActivated;
  private List<String> paymentMethodUsed;
  private boolean tiersPrestationIsActivatedForThisBilling;
  private Locale locale;
  private boolean isReminderActivated;
  private Currency currency;
}
