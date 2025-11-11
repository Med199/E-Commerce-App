package com.ecommerce.project.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.web.WebProperties;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private long categoryId;

     @NotBlank()
     @Size(min=2,message = "Category Name must contain at least 2 Carecters")
     private String categoryName;

}
