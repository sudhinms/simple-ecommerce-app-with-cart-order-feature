package com.ecommerce.app.EcommerceApp.dto.productDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductDetailsUserView {
    private Long id;
    private String name;
    private double price;
    private byte[] image;
}
