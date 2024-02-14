package com.ecommerce.app.EcommerceApp.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"})
@Entity
public class ProductDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Size(min = 2,max = 100)
    @NotBlank
   // @Pattern(regexp = "^[a-zA-Z]+$",message = "Give valid product name")
    private String name;
    @NotNull
    @NotBlank
    private String brand;
    @DecimalMin("1.0")
    private double price;
    @Min(1)
    private int quantity;
    private String imagePath;
    @JsonManagedReference
    @ManyToOne
    private Categories category;
    @OneToMany(
            mappedBy = "productDetails")
    private List<Orders> orders;
}
