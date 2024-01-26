package com.osslot.educorder.domain.service;

import com.osslot.educorder.domain.model.Order;
import com.osslot.educorder.domain.repository.ActivityRepository;
import com.osslot.educorder.domain.repository.OrderRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

  private final ActivityRepository activityRepository;
  private final OrderRepository orderRepository;

  public OrderService(ActivityRepository activityRepository, OrderRepository orderRepository) {
    this.activityRepository = activityRepository;
    this.orderRepository = orderRepository;
  }

  public List<Order> createOrdersForMonth(int year, int month) {
    var activities = activityRepository.findAllByMonth(year, month);
    /* TODO group by patient */
    /* TODO order from activity */
    /* TODO create orders by patient */
    return List.of();
  }
}
