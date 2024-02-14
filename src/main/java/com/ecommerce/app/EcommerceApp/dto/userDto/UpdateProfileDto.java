package com.ecommerce.app.EcommerceApp.dto.userDto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateProfileDto {
    @Nullable
    @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}",message = "Email format is incorrect...")
    private String email;
    @Nullable
    @Size(min = 2,max = 100)
    private String name;
    @Nullable
    @Pattern(regexp="(^$|[0-9]{10})",message = "Invalid mobile number")
    private String mobile;
}
