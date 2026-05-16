package com.webshop.clothes.repository;

import com.webshop.clothes.model.Order;
import com.webshop.clothes.model.OrderStatus;
import com.webshop.clothes.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByUserAndStatus(User user, OrderStatus status);
}
