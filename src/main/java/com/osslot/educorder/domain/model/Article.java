package com.osslot.educorder.domain.model;

import java.math.BigDecimal;

public record Article(String id, String name, BigDecimal unitPrice, String unit) {}
