package com.ecommerce.project.service;

import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.ProductDTO;
import org.springframework.http.ResponseEntity;

public interface ProductService {
    ProductDTO addProduct(ProductDTO productDTO, Long categoryId);
}
