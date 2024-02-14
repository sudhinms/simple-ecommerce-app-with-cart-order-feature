package com.ecommerce.app.EcommerceApp.dto.paymentsDto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class PaymentDto {

    @Pattern(regexp = "^[a-zA-Z]+$",message = "Give valid name")
    private String name;
    @Pattern(regexp="(^[A-Za-z]{3}\\d{8}$)",message = "Invalid account number")
    private String accountNumber;
    @Pattern(regexp = "^\\d{6}$",message = "Invalid pin")
    private String pin;
}
