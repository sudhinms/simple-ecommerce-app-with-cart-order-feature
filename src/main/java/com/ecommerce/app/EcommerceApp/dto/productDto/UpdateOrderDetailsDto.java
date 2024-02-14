package com.ecommerce.app.EcommerceApp.dto.productDto;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateOrderDetailsDto {
    @Nullable
    private String status;
    @Nullable
    private Date expectedDeliveryDate;
    @Nullable
    private String paymentStatus;
}
