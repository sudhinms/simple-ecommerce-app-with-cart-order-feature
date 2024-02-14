package com.ecommerce.app.EcommerceApp.dto.userDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PasswordDto {
    @NotBlank
    private String oldPassword;
    @Size(min = 8,max = 100,message = "Password length must be greater than 8")
    @Pattern(regexp = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$ %^&*-]).{8,}$"
            ,message = "Password must contain upper case, lower case, numbers and special characters")
    private String newPassword;
    @Size(min = 8,max = 100,message = "Password length must be greater than 8")
    @Pattern(regexp = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$ %^&*-]).{8,}$"
            ,message = "Password must contain upper case, lower case, numbers and special characters")
    private String confirmPassword;
}
