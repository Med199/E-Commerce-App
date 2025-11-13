package com.ecommerce.project.service;

import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;

public interface ProductService {
    ProductDTO addProduct(ProductDTO productDTO, Long categoryId);
    ProductResponse getAllProducts();
    ProductResponse getProductsByCategory(Long categoryId);
    ProductResponse getProductsByKeyword(String keyword);
    ProductDTO updateProduct(Long productId, ProductDTO product);
    ProductDTO deleteProduct(Long productId);
}
