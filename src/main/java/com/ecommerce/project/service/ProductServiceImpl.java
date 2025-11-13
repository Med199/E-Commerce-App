package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repositories.CategoryRepository;
import com.ecommerce.project.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

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

    @Override
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
        //Get the existing product from DB
        Product productFromDb= productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product","ProductId",productId));
        Product product= modelMapper.map(productDTO,Product.class);

        //Update the product entities
        productFromDb.setProductName(product.getProductName());
        productFromDb.setDescription(product.getDescription());
        productFromDb.setQuantity(product.getQuantity());
        productFromDb.setDiscount(product.getDiscount());
        productFromDb.setPrice(product.getPrice());
        productFromDb.setSpecialPrice(product.getSpecialPrice());

        //Save to database
        Product savedProduct= productRepository.save(productFromDb);
        return modelMapper.map(savedProduct,ProductDTO.class);
    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        Product productFromDb= productRepository.findById(productId)
                .orElseThrow(()->new ResourceNotFoundException("Product","ProductId",productId));
        productRepository.deleteById(productId);
        return modelMapper.map(productFromDb, ProductDTO.class);
    }
}
