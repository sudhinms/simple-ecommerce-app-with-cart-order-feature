package com.ecommerce.app.EcommerceApp.dto.userDto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AddressDto {
    @NotBlank(message = "Street name cannot be blank")
    private String street;
    @NotBlank(message = "City name cannot be blank")
    private String city;
    @NotBlank(message = "State name cannot be blank")
    private String state;
    @NotBlank(message = "Pin code cannot be blank")
    @Pattern(regexp = "^\\d{6}$",message = "Invalid pin, Pin must contain 6 digits")
    private String pin;
}
