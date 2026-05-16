package com.webshop.clothes.config;

import com.webshop.clothes.model.Product;
import com.webshop.clothes.model.ProductImage;
import com.webshop.clothes.model.Role;
import com.webshop.clothes.model.User;
import com.webshop.clothes.repository.ProductRepository;
import com.webshop.clothes.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader implements ApplicationRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User admin = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
            System.out.println("Admin account aangemaakt: " + adminEmail);
        }

        if (productRepository.count() == 0) {
            List<Product> products = List.of(
                product("Test T-Shirt", "Een klassieker voor elke dag.", new BigDecimal("29.99"), 10,
                    "https://res.cloudinary.com/dlxwua3d6/image/upload/test_tshirt_front_bfehad",
                    "https://res.cloudinary.com/dlxwua3d6/image/upload/test_tshirt_back_nhrwdo"),
                product("broek", "Comfortabele broek voor dagelijks gebruik.", new BigDecimal("50.00"), 10,
                    "https://res.cloudinary.com/dlxwua3d6/image/upload/v1778865964/pants_alo0ln.webp"),
                product("Wit T-Shirt", "Strak wit T-shirt van katoen.", new BigDecimal("19.99"), 50,
                    "https://res.cloudinary.com/dlxwua3d6/image/upload/v1778867011/wit_tshirt_hrkkke.avif"),
                product("Zwart Hoodie", "Warme hoodie voor koele avonden.", new BigDecimal("44.99"), 30),
                product("Spijkerbroek Slim Fit", "Moderne slim fit spijkerbroek.", new BigDecimal("59.99"), 25),
                product("Gestreepte Polo", "Casual polo met gestreept patroon.", new BigDecimal("34.99"), 40),
                product("Witte Sneakers", "Tijdloze witte sneakers.", new BigDecimal("79.99"), 20),
                product("Zwarte Jogger", "Comfortabele jogger voor sport en vrije tijd.", new BigDecimal("39.99"), 35),
                product("Gebloemde Zomerjurk", "Lichte jurk voor warme dagen.", new BigDecimal("49.99"), 15),
                product("Leren Riem", "Stijlvolle leren riem.", new BigDecimal("24.99"), 60)
            );
            productRepository.saveAll(products);
            System.out.println("Seed producten aangemaakt: " + products.size() + " stuks");
        }
    }

    private Product product(String name, String description, BigDecimal price, int stock, String... imageUrls) {
        Product p = Product.builder()
                .name(name)
                .description(description)
                .price(price)
                .stock(stock)
                .build();
        for (String url : imageUrls) {
            p.getImages().add(ProductImage.builder().product(p).productName(name).imageUrl(url).build());
        }
        return p;
    }
}
