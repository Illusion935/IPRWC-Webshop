package com.webshop.clothes.controller;

import com.webshop.clothes.dto.AddToCartRequest;
import com.webshop.clothes.model.Order;
import com.webshop.clothes.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/cart")
    public ResponseEntity<Order> getCart() {
        return ResponseEntity.ok(orderService.getCart());
    }

    @PostMapping("/cart/items")
    public ResponseEntity<Order> addToCart(@RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(orderService.addToCart(request));
    }

    @DeleteMapping("/cart/items/{itemId}")
    public ResponseEntity<Order> removeFromCart(@PathVariable Long itemId) {
        return ResponseEntity.ok(orderService.removeFromCart(itemId));
    }

    @DeleteMapping("/cart")
    public ResponseEntity<Void> clearCart() {
        orderService.clearCart();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/cart/checkout")
    public ResponseEntity<Order> checkout() {
        return ResponseEntity.ok(orderService.checkout());
    }
}
