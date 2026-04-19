package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "product")
@ToString
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long productId;

    @NotBlank
    @Size(min=3,message = "Product Name must contain at least 3 Carecters")
    private String productName;

    @NotBlank
    @Size(min=6,message = "Product description must contain at least 6 Carecters")
    private String description;

    private String image;
    private Integer quantity;
    private double price;
    private double discount;
    private double specialPrice;

    // Category
    @ManyToOne
    @JoinColumn(name="category_id")
    private Category category;

    // User
    @ManyToOne
    @JoinColumn(name="seller_id")
    private User user;

    @OneToMany(mappedBy = "product",cascade = {CascadeType.PERSIST,CascadeType.REMOVE,CascadeType.MERGE},fetch=FetchType.LAZY)
    private List<CartItem> CartItems = new ArrayList<>();

}
