package com.ecommerce.app.EcommerceApp.dto.productDto;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductDetailsDto {
    @Nullable
    private Long id;
    private String name;
    private String brand;
    private double price;
    private int quantity;
    @Nullable
    private byte[] ProductImage;
    @Nullable
    private String category;
}
