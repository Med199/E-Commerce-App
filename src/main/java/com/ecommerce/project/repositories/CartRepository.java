package com.ecommerce.project.repositories;

import com.ecommerce.project.model.Cart;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart,Long> {
    // JPA Can't define findCartByEmail itself,
    // because user.email is a nested object field of Cart (not its own field )
    @Query("SELECT ca FROM Cart ca WHERE ca.user.email= ?1")
    Cart findCartByEmail(String email);

    @Query("SELECT c FROM Cart c JOIN FETCH c.cartItems ci JOIN FETCH ci.product p WHERE p.id = ?1")
    List<Cart> findCartsByProductId(Long productId);
}
