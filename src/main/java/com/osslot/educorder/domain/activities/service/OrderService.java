package com.osslot.educorder.domain.activities.service;

import com.osslot.educorder.domain.activities.model.Order;
import com.osslot.educorder.domain.activities.repository.ActivityRepository;
import com.osslot.educorder.domain.activities.repository.OrderRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

  private final ActivityRepository firestoreActivityRepository;
  private final OrderRepository orderRepository;

  public OrderService(
      ActivityRepository firestoreActivityRepository, OrderRepository orderRepository) {
    this.firestoreActivityRepository = firestoreActivityRepository;
    this.orderRepository = orderRepository;
  }

  public List<Order> createOrdersForMonth(int year, int month) {
    //    var activities = firestoreActivityRepository.findAllByMonth(year, month);
    /* TODO group by patient */
    /* TODO order from activity */
    /* TODO create orders by patient */
    return List.of();
  }
}
