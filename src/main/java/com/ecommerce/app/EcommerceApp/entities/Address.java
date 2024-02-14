package com.ecommerce.app.EcommerceApp.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"})
@Entity
@ToString
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String street;
    @Size(min = 2,max = 100)
    @Pattern(regexp = "^[a-zA-Z ]+$",message = "Give valid city")
    private String city;
    @Size(min = 2,max = 100)
    @Pattern(regexp = "^[a-zA-Z ]+$",message = "Give valid state")
    private String state;
    @Pattern(regexp = "^\\d{6}$",message = "Invalid pin")
    private String pin;
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    @JoinColumn(name = "user_id")
    private UserInfo userInfo;
    @JsonIgnore
    @OneToMany(orphanRemoval = false,
            mappedBy = "address")
    private List<Orders> order;
}
