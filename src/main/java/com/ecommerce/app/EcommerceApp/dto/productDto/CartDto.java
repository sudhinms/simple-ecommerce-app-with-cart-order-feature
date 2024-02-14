package com.ecommerce.app.EcommerceApp.dto.productDto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CartDto {
    private long productId;
    private String productName;
    private double price;
    @Min(1)
    private int quantity;
    private byte[] image;

}
