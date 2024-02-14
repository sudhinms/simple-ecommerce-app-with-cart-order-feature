package com.ecommerce.app.EcommerceApp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.sql.Date;
import java.time.LocalDateTime;

@NoArgsConstructor
@Data
@Entity
public class Orders {
    @EmbeddedId
    private OrderPrimaryKey orderKeyId;
    private LocalDateTime orderDateTime;
    @Future(message = "Date must be in the past")
    private Date expectedDeliveryDate;
    @Min(value = 1)
    private int quantity;
    @ManyToOne
    @JoinColumn(name = "address_id")
    private Address address;
    private String status;
    @ManyToOne
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserInfo userInfo;
    @ManyToOne
    @JoinColumn(name = "product_id")
    private ProductDetails productDetails;
    private String paymentStatus;
    private double totalPrice;
    private String paymentNumber;

}
