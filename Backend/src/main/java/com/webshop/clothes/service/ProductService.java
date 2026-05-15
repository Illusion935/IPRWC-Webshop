package com.webshop.clothes.service;

import com.webshop.clothes.dto.ProductImageRequest;
import com.webshop.clothes.dto.ProductRequest;
import com.webshop.clothes.model.Product;
import com.webshop.clothes.model.ProductImage;
import com.webshop.clothes.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> getAll() {
        return productRepository.findAll();
    }

    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product niet gevonden: " + id));
    }

    public Product create(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .build();
        return productRepository.save(product);
    }

    public Product update(Long id, ProductRequest request) {
        Product product = getById(id);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        return productRepository.save(product);
    }

    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    public ProductImage addImage(Long productId, String imageUrl) {
        Product product = getById(productId);

        ProductImage image = ProductImage.builder()
                .product(product)
                .productName(product.getName())
                .imageUrl(imageUrl)
                .build();

        product.getImages().add(image);
        productRepository.save(product);
        return image;
    }

    public List<ProductImage> addImages(Long productId, List<ProductImageRequest> requests) {
        return requests.stream()
                .map(req -> addImage(productId, req.getImageUrl()))
                .toList();
    }

    public void deleteImage(Long productId, Long imageId) {
        Product product = getById(productId);
        product.getImages().removeIf(img -> img.getId().equals(imageId));
        productRepository.save(product);
    }
}
