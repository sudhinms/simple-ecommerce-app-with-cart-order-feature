package com.ecommerce.app.EcommerceApp.dto.userDto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserInfoDto {

    @NotNull
    @NotBlank
    @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}",message = "Email format is incorrect...")
    private String email;
    @Size(min = 2,max = 100)
//    @Pattern(regexp = "^[a-zA-Z]+$")
    @NotNull(message = "Name cannot be null")
    @NotBlank
    private String name;
    @Size(min = 8,max = 100,message = "Password length must be greater than 8")
    @NotNull(message = "Password cannot be null")
    @NotBlank
    @Pattern(regexp = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$ %^&*-]).{8,}$"
            ,message = "Password must contain upper case, lower case, numbers and special characters")
    private String password;
    @NotNull(message = "Password cannot be null")
    @NotBlank
    @Pattern(regexp = "^\\d{10}$", message = "Invalid mobile number")
    private String mobile;
    @Nullable
    private byte[] profileImage;
}
