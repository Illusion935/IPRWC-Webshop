package com.webshop.clothes.service;

import com.webshop.clothes.dto.AddToCartRequest;
import com.webshop.clothes.model.*;
import com.webshop.clothes.repository.OrderRepository;
import com.webshop.clothes.repository.ProductRepository;
import com.webshop.clothes.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow();
    }

    public Order getCart() {
        User user = getCurrentUser();
        return orderRepository.findByUserAndStatus(user, OrderStatus.CART)
                .orElseGet(() -> createCart(user));
    }

    @Transactional
    public Order addToCart(AddToCartRequest request) {
        User user = getCurrentUser();
        Order cart = orderRepository.findByUserAndStatus(user, OrderStatus.CART)
                .orElseGet(() -> createCart(user));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product niet gevonden"));

        // Als het product al in het mandje zit, hoog de hoeveelheid op
        cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .ifPresentOrElse(
                        item -> item.setQuantity(item.getQuantity() + request.getQuantity()),
                        () -> cart.getItems().add(OrderItem.builder()
                                .order(cart)
                                .product(product)
                                .quantity(request.getQuantity())
                                .build())
                );

        return orderRepository.save(cart);
    }

    @Transactional
    public Order removeFromCart(Long itemId) {
        User user = getCurrentUser();
        Order cart = orderRepository.findByUserAndStatus(user, OrderStatus.CART)
                .orElseThrow(() -> new RuntimeException("Winkelmandje niet gevonden"));

        cart.getItems().removeIf(item -> item.getId().equals(itemId));
        return orderRepository.save(cart);
    }

    @Transactional
    public void clearCart() {
        User user = getCurrentUser();
        orderRepository.findByUserAndStatus(user, OrderStatus.CART)
                .ifPresent(cart -> {
                    cart.getItems().clear();
                    orderRepository.save(cart);
                });
    }

    @Transactional
    public Order checkout() {
        User user = getCurrentUser();
        Order cart = orderRepository.findByUserAndStatus(user, OrderStatus.CART)
                .orElseThrow(() -> new RuntimeException("Winkelmandje niet gevonden"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Winkelmandje is leeg");
        }

        cart.setStatus(OrderStatus.PLACED);
        return orderRepository.save(cart);
    }

    private Order createCart(User user) {
        return orderRepository.save(Order.builder()
                .user(user)
                .build());
    }
}
