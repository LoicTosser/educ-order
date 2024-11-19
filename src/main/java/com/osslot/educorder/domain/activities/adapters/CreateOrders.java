package com.osslot.educorder.domain.activities.adapters;

import com.osslot.educorder.domain.activities.model.Order;
import com.osslot.educorder.domain.activities.service.OrderService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CreateOrders {

  private final OrderService orderService;

  public CreateOrders(OrderService orderService) {
    this.orderService = orderService;
  }

  public List<Order> createOrders(int year, int month) {
    return orderService.createOrdersForMonth(year, month);
  }
}
