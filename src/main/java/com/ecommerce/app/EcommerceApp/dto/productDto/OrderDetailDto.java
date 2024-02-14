package com.ecommerce.app.EcommerceApp.dto.productDto;

import com.ecommerce.app.EcommerceApp.dto.userDto.UserDetailsAdminView;
import com.ecommerce.app.EcommerceApp.entities.Address;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class OrderDetailDto {
    private long id;
    private long cartId;
    private LocalDateTime orderDateTime;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date expectedDeliveryDate;
    @NotNull
    private String status;
    private int quantity;
    private ProductDetailsDto productDetails;
    private UserDetailsAdminView UserDetails;
    private Address address;
    private String paymentStatus;
}
