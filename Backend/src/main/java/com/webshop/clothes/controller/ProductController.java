package com.webshop.clothes.controller;

import com.webshop.clothes.dto.ProductImageRequest;
import com.webshop.clothes.dto.ProductRequest;
import com.webshop.clothes.model.Product;
import com.webshop.clothes.model.ProductImage;
import com.webshop.clothes.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // Publiek toegankelijk (zie SecurityConfig)
    @GetMapping
    public List<Product> getAll() {
        return productService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    // Alleen ADMIN (zie SecurityConfig)
    @PostMapping
    public ResponseEntity<Product> create(@RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable Long id, @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Image endpoints — ook alleen ADMIN (POST/DELETE vallen onder /api/products/**)
    @PostMapping("/{id}/images")
    public ResponseEntity<ProductImage> addImage(
            @PathVariable Long id,
            @RequestBody ProductImageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.addImage(id, request.getImageUrl()));
    }

    @PostMapping("/{id}/images/bulk")
    public ResponseEntity<List<ProductImage>> addImages(
            @PathVariable Long id,
            @RequestBody List<ProductImageRequest> requests) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.addImages(id, requests));
    }

    @DeleteMapping("/{id}/images/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long id, @PathVariable Long imageId) {
        productService.deleteImage(id, imageId);
        return ResponseEntity.noContent().build();
    }
}
