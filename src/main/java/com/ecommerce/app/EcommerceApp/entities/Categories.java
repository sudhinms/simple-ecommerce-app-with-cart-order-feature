package com.ecommerce.app.EcommerceApp.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class Categories {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(unique = true)
    @NotBlank
    private String name;
    @JsonBackReference
    @OneToMany(mappedBy = "category",
            fetch = FetchType.LAZY,
            cascade = {CascadeType.MERGE,CascadeType.PERSIST})
    private List<ProductDetails> productDetails;
}
