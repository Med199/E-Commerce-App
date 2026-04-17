package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "addresses")
@Data
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    @NotBlank
    @Size(min = 5, message = "Street name must be at least 5 characters")
    private String street;

    @NotBlank
    @Size(min = 4, message = "Building name must be at least 4 characters")
    private String buildingName;

    @NotBlank
    @Size(min = 2, message = "State name must be at least 2 characters")
    private String state;

    @NotBlank
    @Size(min = 2, message = "Country name must be at least 2 characters")
    private String country;

    @NotBlank
    @Size(min = 6, message = "Pin code must be at least 2 characters")
    private String pinCode;

    // Users
    @ToString.Exclude // avoid showing addresses while using ToString
    @ManyToMany(mappedBy="addresses")
    private List<User> users = new ArrayList<>();

    public Address(String street, String buildingName, String state, String country, String pinCode) {
        this.street = street;
        this.buildingName = buildingName;
        this.state = state;
        this.country = country;
        this.pinCode = pinCode;
    }
}
