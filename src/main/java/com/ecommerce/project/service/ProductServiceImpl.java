package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repositories.CategoryRepository;
import com.ecommerce.project.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public ProductDTO addProduct(ProductDTO productDTO, Long categoryId) {
        Category category=categoryRepository.findById(categoryId)
                .orElseThrow(()->new ResourceNotFoundException("Category","categoryId",categoryId));

        // convert the ProductDTO to Product
        Product product= modelMapper.map(productDTO,Product.class);

        //Set the product properties
        product.setCategory(category);
        product.setImage("default.png");
        double specialPrice = product.getPrice()- ((product.getDiscount()*0.01)*product.getPrice());
        product.setSpecialPrice(specialPrice);

        Product savedProduct= productRepository.save(product);
        return modelMapper.map(savedProduct,ProductDTO.class);
    }

    @Override
    public ProductResponse getAllProducts() {
        List<Product> products = productRepository.findAll();

        // convert the products into productsDTOs
        List<ProductDTO> productsDTO= products.stream()
                .map(product -> modelMapper.map(product,ProductDTO.class))
                .toList();

        // return ProductResponse
        ProductResponse productResponse= new ProductResponse();
        productResponse.setContent(productsDTO);
        return productResponse;
    }

    @Override
    public ProductResponse getProductsByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(()-> new ResourceNotFoundException("Category","categoryId",categoryId));
        List<Product> products= productRepository.findByCategoryOrderByPriceAsc(category);

        // convert the products into productsDTOs
        List<ProductDTO> productsDTO = products.stream()
                .map(product -> modelMapper.map(product,ProductDTO.class))
                .toList();

        // return ProductResponse
        ProductResponse productResponse= new ProductResponse();
        productResponse.setContent(productsDTO);
        return productResponse;
    }

    @Override
    public ProductResponse getProductsByKeyword(String keyword) {

        List<Product> products= productRepository.findByProductNameLikeIgnoreCase('%'+ keyword+'%');

        // convert the products into productsDTOs
        List<ProductDTO> productsDTO = products.stream()
                .map(product -> modelMapper.map(product,ProductDTO.class))
                .toList();

        // return ProductResponse
        ProductResponse productResponse= new ProductResponse();
        productResponse.setContent(productsDTO);
        return productResponse;
    }
}
