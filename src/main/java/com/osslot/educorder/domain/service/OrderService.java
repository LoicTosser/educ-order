package com.osslot.educorder.domain.service;

import com.osslot.educorder.domain.model.Order;
import com.osslot.educorder.domain.repository.ActivityRepository;
import com.osslot.educorder.domain.repository.OrderRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

  private final ActivityRepository googleSheetActivityRepository;
  private final OrderRepository orderRepository;

  public OrderService(
      ActivityRepository googleSheetActivityRepository, OrderRepository orderRepository) {
    this.googleSheetActivityRepository = googleSheetActivityRepository;
    this.orderRepository = orderRepository;
  }

  public List<Order> createOrdersForMonth(int year, int month) {
    var activities = googleSheetActivityRepository.findAllByMonth(year, month);
    /* TODO group by patient */
    /* TODO order from activity */
    /* TODO create orders by patient */
    return List.of();
  }
}
