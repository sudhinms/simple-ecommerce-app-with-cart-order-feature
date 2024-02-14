package com.ecommerce.app.EcommerceApp.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"})
@Entity
@Builder
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"email"})})
public class UserInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @NotNull
    @NotBlank
    @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}",message = "Email format is incorrect...")
    @Column(unique = true)
    private String email;
    @Size(min = 2,max = 100)
//    @Pattern(regexp = "^[a-zA-Z]+$",message = "Give valid name")
    private String name;
    @Size(min = 10,max = 270)
    @NotNull
    private String password;
    private String role;
    @Pattern(regexp="(^$|[0-9]{10})",message = "Invalid mobile number")
    private String mobile;
    @Nullable
    @Lob
    @Column(name = "profile_image",length = 3145728)
    private byte[] profileImage;

    @JsonManagedReference
    @OneToMany(mappedBy = "userInfo",
            fetch = FetchType.LAZY,
            cascade = CascadeType.REMOVE)
    private List<Address> addresses;
    @OneToMany(cascade = CascadeType.ALL,
            mappedBy = "userInfo")
    private List<Orders> orders;
    @OneToMany(mappedBy = "userInfo")
    private List<CartDetails> cartDetails;
}
