package com.osslot.educorder.domain.activities.repository;

import com.osslot.educorder.domain.activities.model.Order;

public interface OrderRepository {

  Order add(Order order);
}
