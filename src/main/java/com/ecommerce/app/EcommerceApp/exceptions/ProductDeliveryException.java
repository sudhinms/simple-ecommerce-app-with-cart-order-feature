package com.ecommerce.app.EcommerceApp.exceptions;

import lombok.Getter;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ProductDeliveryException extends RuntimeException {

    @Getter
    private static Link link;

    public ProductDeliveryException(String message) {
        super(message);
    }

    public ProductDeliveryException(String message,Link link) {
        super(message);
        ProductDeliveryException.link=link;
    }

}
