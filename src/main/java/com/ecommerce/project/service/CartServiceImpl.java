package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.repositories.CartItemRepository;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.util.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.modelmapper.internal.bytebuddy.implementation.bytecode.Throw;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    ModelMapper modelMapper;


    @Autowired
    private AuthUtil authUtil;

    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {

        //*** Check if Cart already exists, otherwise create new Cart ***
        Cart cart = createCart();

        //*** Retrieve Product Details ***
        Product product = productRepository.findById(productId)
                .orElseThrow(()->new ResourceNotFoundException("Product","ProductId",productId));

        //*** Perform Validations ***
        //check if product already exists in the cart
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(
                cart.getCartId(),
                productId
        );
        if(cartItem != null){
            throw new APIException("Product "+ product.getProductName()+" Already exists in your Shopping cart");
        }
        //check quantity of the product
        if(product.getQuantity() == 0){
            throw new APIException(product.getProductName()+" is not available");
        }
        //check if the ordered quantity match the remain quantity
        if(product.getQuantity() < quantity){
            throw new APIException("Please, make an order of the "+ product.getProductName()
                    + "less than or equal to the quantity "+ product.getQuantity() + ".");
        }

        //*** Create Cart Item ***
        CartItem newCartItem = new CartItem();
        newCartItem.setProduct(product);
        newCartItem.setCart(cart);
        newCartItem.setQuantity(quantity);
        newCartItem.setDiscount(product.getDiscount());
        newCartItem.setProductPrice(product.getSpecialPrice());

        //*** Save Cart_Item && update Cart***
        cartItemRepository.save(newCartItem);
        cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice() * quantity));
        cartRepository.save(cart);

        //*** Return updated Cart ***
        CartDTO cartDTO = modelMapper.map(cart,CartDTO.class);

        // update ProductsDTO of CartDTO
        List<CartItem> cartItems = cart.getCartItems();
        Stream<ProductDTO> productsDTO = cartItems.stream().map( item ->{
            ProductDTO productDTO = modelMapper.map(item.getProduct(),ProductDTO.class);
            productDTO.setQuantity(item.getQuantity());
            return productDTO;
        });

        cartDTO.setProducts(productsDTO.toList());
        return cartDTO;
    }

    private Cart createCart(){
        Cart userCart = cartRepository.findCartByEmail(authUtil.loggedInEmail());
        if ( userCart != null){
            return userCart;
        }
        Cart cart = new Cart();
        cart.setTotalPrice(0.00);
        cart.setUser(authUtil.loggedInUser());
        return cartRepository.save(cart);
    }

    // For Admin side to get all the carts of the users
    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();
        if(carts.isEmpty()){
            throw new APIException("No Carts exist");
        }

        List<CartDTO> cartsDTO = carts.stream()
                .map(cart-> {
                    CartDTO cartDTO = modelMapper.map(cart,CartDTO.class);
                    List<ProductDTO> productsDTO = cart.getCartItems().stream()
                            .map(p ->{
                                ProductDTO productDTO = modelMapper.map(p.getProduct(),ProductDTO.class);
                                //override quantity with ORDERED quantity
                                productDTO.setQuantity(p.getQuantity());
                                return productDTO;
                            }).collect(Collectors.toList());
                    cartDTO.setProducts(productsDTO);
                    return cartDTO;
                }).toList();
        return cartsDTO;
    }

    @Override
    public CartDTO getUsersCart() {
        String email = authUtil.loggedInEmail();
        Cart cart = cartRepository.findCartByEmail(email);
        if(cart == null){
            throw new  ResourceNotFoundException("User-Cart","email",email);
        }
        CartDTO cartDTO = modelMapper.map(cart,CartDTO.class);

        List<ProductDTO> productsDTO = cart.getCartItems().stream()
                .map(cartItem -> {
                    ProductDTO productDTO =
                            modelMapper.map(cartItem.getProduct(), ProductDTO.class);
                    // override quantity with ORDERED quantity
                    productDTO.setQuantity(cartItem.getQuantity());
                    return productDTO;
                }).toList();
        cartDTO.setProducts(productsDTO);
        return cartDTO;
    }

    @Override
    @Transactional // ensure that all operations are executed in the method. if any exception appears the previous operation will rolled back
    public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {
        // get user details && its cart
        String email = authUtil.loggedInEmail();

        Cart cart = cartRepository.findCartByEmail(email);
        if (cart == null) {
            throw new ResourceNotFoundException("UserCart", "email", email);
        }

        // get the product
        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("product","productId",productId));

        if(product.getQuantity() == 0){
            throw new APIException(product.getProductName()+ " is not available");
        }

        //check if the ordered quantity match the remain quantity
        if(product.getQuantity() < quantity){
            throw new APIException("Please, make an order of the "+ product.getProductName()
                    + "less than or equal to the quantity "+ product.getQuantity() + ".");
        }

        // check if the product is in the cart or not
        Long cartId = cart.getCartId();
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId,productId);
        if(cartItem == null){
            throw new APIException("Product " + product.getProductName() + " Not available in the cart!");
        }

        // avoid negative quantity after reduce
        int newQuantity = cartItem.getQuantity() + quantity;
        if(newQuantity<0){
            throw new APIException("The quantity cannot be negative");
        }

        // delete the product if newQuantity is zero
        if(newQuantity == 0){
            deleteProductFromCart(cartId,productId);
        }else {
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setDiscount(product.getDiscount());
            cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getProductPrice() * quantity));
            cartRepository.save(cart);
        }

        CartItem updatedItem = cartItemRepository.save(cartItem);
        if( updatedItem.getQuantity() == 0){
            cartItemRepository.deleteById(updatedItem.getCartItemId());
        }

        CartDTO cartDTO = modelMapper.map(cart,CartDTO.class);
        List<CartItem> cartItems = cart.getCartItems();

        Stream<ProductDTO> productDTOS = cartItems.stream().map(item->{
            ProductDTO prd = modelMapper.map(item.getProduct(),ProductDTO.class);
            prd.setQuantity(item.getQuantity());
            return prd;
        });
        cartDTO.setProducts(productDTOS.toList());
        return cartDTO;
    }

    @Transactional // ensure that all operations are executed in the method. if any exception appears the previous operation will rolled back
    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(()-> new ResourceNotFoundException("Cart","cartId",cartId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId,productId);
        if(cartItem == null){
            throw new ResourceNotFoundException("product","productId",productId);
        }
        // Update the total price of the cart
        cart.setTotalPrice(cart.getTotalPrice() -
                (cartItem.getProductPrice() * cartItem.getQuantity()));

        //delete the product from the cart
        cartItemRepository.deleteCartItemByProductIdAndCartId(cartId,productId);
        return "Product " + cartItem.getProduct().getProductName() + " removed from the cart";
    }

    @Override
    public void updateProductInCarts(Long cartId, Product savedProduct) {
        // Some validations
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow( ()-> new ResourceNotFoundException("Cart","cartId",cartId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId,savedProduct.getProductId());
        if(cartItem == null){
            throw new APIException("Product " + savedProduct.getProductName() + "not available in the cart");
        }

        //*** update the total price of the cart ***
        //  Update the price of the product on CartItem
        cartItem.setProductPrice(savedProduct.getSpecialPrice());
        cartItemRepository.save(cartItem);

        // reload the cartItems
        List<CartItem> updatedCartItems = cartItemRepository.findByCart_CartId(cartId);

        // Recalculate the total price
        double newTotal = updatedCartItems.stream()
                .mapToDouble(item -> item.getProductPrice() * item.getQuantity())
                .sum();
        // save the cart
        cart.setTotalPrice(newTotal);
        cartRepository.save(cart);
    }
}
