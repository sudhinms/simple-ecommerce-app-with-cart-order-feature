package com.ecommerce.app.EcommerceApp.dto.userDto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserDetailsAdminView {
    @NotBlank
    private String email;
    @Size(min = 2,max = 100)
    @Pattern(regexp = "^[a-zA-Z]+$")
    private String name;
    @Pattern(regexp="(^$|[0-9]{10})",message = "Invalid mobile number")
    @NotBlank
    private String mobile;
    @Nullable
    private byte[] profileImage;
}
