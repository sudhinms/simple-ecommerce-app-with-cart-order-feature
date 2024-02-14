package com.ecommerce.app.EcommerceApp.entities;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
@Table(name = "cart")
public class CartDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne
    @JoinColumn(name = "product_id")
    private ProductDetails productDetails;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserInfo userInfo;
    private int quantity;
}
