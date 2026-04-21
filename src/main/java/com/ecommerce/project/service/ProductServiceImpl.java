package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.CategoryRepository;
import com.ecommerce.project.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileService fileService;

    @Value("${project.image}")
    private String path;

    @Override
    public ProductDTO addProduct(ProductDTO productDTO, Long categoryId) {
        Category category=categoryRepository.findById(categoryId)
                .orElseThrow(()->new ResourceNotFoundException("Category","categoryId",categoryId));

        // check if the productName already exists
        boolean isProductNotExist=true;
        List<Product> products = category.getProducts();

        for (Product value : products) {
            if (value.getProductName().equals(productDTO.getProductName())) {
                isProductNotExist = false;
                break;
            }
        }

        if(isProductNotExist) {
            // convert the ProductDTO to Product
            Product product = modelMapper.map(productDTO, Product.class);

            //Set the product properties
            product.setCategory(category);
            product.setImage("default.png");
            double specialPrice = product.getPrice() - ((product.getDiscount() * 0.01) * product.getPrice());
            product.setSpecialPrice(specialPrice);

            Product savedProduct = productRepository.save(product);
            return modelMapper.map(savedProduct, ProductDTO.class);
        }else{
            throw new APIException("Product already exist!");
        }
    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize,String sortBy, String sortOrder) {
        Sort sortByAndOrder=sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails= PageRequest.of(pageNumber,pageSize, sortByAndOrder);
        Page<Product> pageProducts= productRepository.findAll(pageDetails);
        List<Product> products = pageProducts.getContent();

        // convert the products into productsDTOs
        List<ProductDTO> productsDTO= products.stream()
                .map(product -> modelMapper.map(product,ProductDTO.class))
                .toList();

        if(products.isEmpty()){
            throw new APIException("Oops, products Found!");
        }

        // return ProductResponse
        ProductResponse productResponse= new ProductResponse();
        productResponse.setContent(productsDTO);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setLastPage(pageProducts.isLast());
        return productResponse;
    }

    @Override
    public ProductResponse getProductsByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(()-> new ResourceNotFoundException("Category","categoryId",categoryId));

        Sort sortByAndOrder=sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails= PageRequest.of(pageNumber,pageSize, sortByAndOrder);
        Page<Product> pageProducts= productRepository.findByCategoryOrderByPriceAsc(category, pageDetails);
        List<Product> products = pageProducts.getContent();

        // convert the products into productsDTOs
        List<ProductDTO> productsDTO = products.stream()
                .map(product -> modelMapper.map(product,ProductDTO.class))
                .toList();

        if(products.isEmpty()){
            throw new APIException("There is no products in Category: "+category.getCategoryName());
        }

        // return ProductResponse
        ProductResponse productResponse= new ProductResponse();
        productResponse.setContent(productsDTO);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setLastPage(pageProducts.isLast());
        return productResponse;
    }

    @Override
    public ProductResponse getProductsByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder=sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails= PageRequest.of(pageNumber,pageSize, sortByAndOrder);
        Page<Product> pageProducts= productRepository.findByProductNameLikeIgnoreCase('%'+ keyword+'%', pageDetails);
        List<Product> products= pageProducts.getContent();

        // convert the products into productsDTOs
        List<ProductDTO> productsDTO = products.stream()
                .map(product -> modelMapper.map(product,ProductDTO.class))
                .toList();

        if(products.isEmpty()){
            throw new APIException("There is no products with keyword: "+keyword);
        }

        // return ProductResponse
        ProductResponse productResponse= new ProductResponse();
        productResponse.setContent(productsDTO);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setLastPage(pageProducts.isLast());
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
        double specialPrice = product.getPrice() - ((product.getDiscount() * 0.01) * product.getPrice());
        product.setSpecialPrice(specialPrice);
        productFromDb.setSpecialPrice(product.getSpecialPrice());

        //Save to database
        Product savedProduct= productRepository.save(productFromDb);

        // update the product in the all the existing carts too
        List<Cart> carts = cartRepository.findCartsByProductId(productId);
        carts.forEach(cart -> cartService.updateProductInCarts(cart.getCartId(),savedProduct));
        return modelMapper.map(savedProduct,ProductDTO.class);
    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
//        Product productFromDb= productRepository.findById(productId)
//                .orElseThrow(()->new ResourceNotFoundException("Product","ProductId",productId));
//
//        // delete the product from the cart before deleting from database
//        List<Cart> carts = cartRepository.findCartsByProductId(productId);
//        carts.forEach(cart -> cartService.deleteProductFromCart(cart.getCartId(),productId));
//
//        productRepository.deleteById(productId);
//        return modelMapper.map(productFromDb, ProductDTO.class);

        // Load product (must exist)
        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product", "ProductId", productId));

        // Find all carts that contain this product
        List<Cart> carts = cartRepository.findCartsByProductId(productId);

        // Remove product from each cart + recalculate total
        for (Cart cart : carts) {
            cart.getCartItems().removeIf(item ->
                    item.getProduct().getProductId().equals(productId)
            );
            double total = 0.0;
            for (CartItem item : cart.getCartItems()) {
                total += item.getProductPrice() * item.getQuantity();
            }
            cart.setTotalPrice(total);
        }

        // Save updated carts
        cartRepository.saveAll(carts);

        // Delete product
        productRepository.deleteById(productId);

        // Return DTO
        return modelMapper.map(productFromDb, ProductDTO.class);

    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
        // Get the Product from DB
        Product productFromDb= productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product","ProductId",productId));

        // Upload image to the server, and get the new file name of uploaded image
        String fileName=fileService.uploadImage(path,image);

        // Update the new file name to the product
        productFromDb.setImage(fileName);
        Product updatedProduct= productRepository.save(productFromDb);

        // return productDTO
        return modelMapper.map(updatedProduct, ProductDTO.class);
    }
}
