package com.osslot.educorder.infrastructure.repository.abby;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

@AllArgsConstructor
@Getter
@Builder
@NoArgsConstructor
@Setter
public class Billing {
    @JsonProperty("_id")
    private String id;
    private String companyId;
    private Integer billingType;
    private String title;
    private Integer billingState;
    private List<Product> product;
    private ZonedDateTime date;
    private ZonedDateTime dueDate;
    private Customer customer;
    private String opportunityId;
    private BankInformation bankInformation;
    private Reminder reminder;
    private PaymentDelay paymentDelay;
    private LatePenalty latePenalty;
    private LumpSumCompensation lumpSumCompensation;
    private DiscountAdvancePayment discountAdvancePayment;
    private String headerNote;
    private String footerNote;
    private boolean displayGoodForApproval;
    private boolean displayAbbyLogo;
    private boolean displayLegalStatus;
    private boolean displayRequiredMentionsProduct;
    private List<PaymentMethodUsed> paymentMethodUsed;
    private boolean useStripePayment;
    private List<String> additionalLogos;
    private List<String> attachments;
//    private Style style;
    private String locale;
    private Integer template;
    private boolean test;
    private boolean tiersPrestationIsActivatedForThisBilling;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private ZonedDateTime finalizedAt;
    private String number;
    private FinalizedFile finalizedFile;
    private ZonedDateTime paidAt;

    record LatePenalty(
            Integer value
    ) {}
    record LumpSumCompensation(
            Integer value
    ) {}
    record DiscountAdvancePayment(
            Integer value
    ) {}
    record FinalizedFile(String id,
            String url, String relativeUrl) {}
}
