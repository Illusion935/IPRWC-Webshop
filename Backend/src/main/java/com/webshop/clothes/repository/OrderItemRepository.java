package com.webshop.clothes.repository;

import com.webshop.clothes.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    void deleteByProductId(Long productId);
}
