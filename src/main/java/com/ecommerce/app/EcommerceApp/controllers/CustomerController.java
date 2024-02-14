package com.ecommerce.app.EcommerceApp.controllers;

import com.ecommerce.app.EcommerceApp.configuration.JwtService;
import com.ecommerce.app.EcommerceApp.dto.paymentsDto.PaymentDto;
import com.ecommerce.app.EcommerceApp.services.CartService;
import com.ecommerce.app.EcommerceApp.services.InvoiceGeneratorService;
import com.ecommerce.app.EcommerceApp.services.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/app/home")
public class CustomerController {

    @Autowired
    private CartService cartService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private OrderService orderService;
    private String currentUserEmail;

    @Autowired
    private InvoiceGeneratorService invoiceGeneratorService;

    @PostMapping("/cart/add/{id}/{quantity}")
    public ResponseEntity<?> addProductToCart(@PathVariable("id") long id,
                                              @RequestHeader (name="Authorization") String token,
                                              @PathVariable("quantity") int quantity){
        this.currentUserEmail=jwtService.extractUsernameFromToken(token.substring(7));
        return cartService.addToCart(id,currentUserEmail,quantity);
    }
    @GetMapping("/cart/getAll")
    public ResponseEntity<?> getAllProductsFromCart(@RequestHeader (name="Authorization") String token){
        this.currentUserEmail=jwtService.extractUsernameFromToken(token.substring(7));
        return cartService.getAllItemsInCart(currentUserEmail);
    }

    @DeleteMapping("/cart/delete/{productId}")
    public ResponseEntity<?> deleteOneFromCart(@PathVariable("productId") long productId,
                                               @RequestHeader (name="Authorization") String token){
        this.currentUserEmail=jwtService.extractUsernameFromToken(token.substring(7));
        return cartService.deleteFromCart(productId,currentUserEmail);
    }
    @GetMapping("/order/getAll")
    public ResponseEntity<?> getAllOrders(@RequestHeader (name="Authorization") String token){
        this.currentUserEmail=jwtService.extractUsernameFromToken(token.substring(7));
        return orderService.getAllOrdersOfUser(currentUserEmail);
    }
//    @PostMapping("/order/create-order/{productId}/{quantity}")
//    public ResponseEntity<?> createOrder(@PathVariable("productId") long productId,
//                                         @PathVariable("quantity") int quantity,
//                                         @RequestHeader (name="Authorization") String token){
//        this.currentUserEmail=jwtService.extractUsernameFromToken(token.substring(7));
//        return orderService.orderSingleItem(currentUserEmail,productId,quantity);
//    }

    @PostMapping("/order/checkout-from-cart")
    public ResponseEntity<?> checkoutCart(@RequestHeader (name="Authorization") String token){
        this.currentUserEmail=jwtService.extractUsernameFromToken(token.substring(7));
        return orderService.checkoutAllProductsInCart(currentUserEmail);
    }

    @PostMapping("/order/confirm-address/{addressId}")
    public ResponseEntity<?> confirmUserAddress(@RequestHeader (name="Authorization") String token,
                                                               @PathVariable("addressId") long addressId){
        this.currentUserEmail=jwtService.extractUsernameFromToken(token.substring(7));
        return orderService.confirmAddress(addressId,currentUserEmail);
    }

    @PostMapping("/order/payment/upi")
    public ResponseEntity<?> initiateUpiPayment(@RequestHeader (name="Authorization") String token,
                                                @RequestBody @Valid PaymentDto paymentDto){
    this.currentUserEmail=jwtService.extractUsernameFromToken(token.substring(7));
    return orderService.upiPayment(currentUserEmail,paymentDto);
}

    @PostMapping("/order/payment/cash-on-delivery")
    public ResponseEntity<?> initiateCashOnDelivery(@RequestHeader (name="Authorization") String token){
        this.currentUserEmail=jwtService.extractUsernameFromToken(token.substring(7));
        return orderService.cashOnDeliveryPayment(currentUserEmail);
    }

    @GetMapping("/order/download-invoice/{orderId}/{cartId}")
    public ResponseEntity<byte[]> downloadInvoice(@RequestHeader (name="Authorization") String token,
                                                  @PathVariable("orderId") long orderId,
                                                  @PathVariable("cartId") long cartId){
        this.currentUserEmail=jwtService.extractUsernameFromToken(token.substring(7));
        byte[]invoiceContext=invoiceGeneratorService.generateInvoice(currentUserEmail,orderId,cartId);
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_PDF);
        httpHeaders.setContentDispositionFormData("attachment","invoice.pdf");
        return new ResponseEntity<>(invoiceContext,httpHeaders, HttpStatus.OK);
    }

    @PatchMapping("/order/return-order/{orderId}/{cartId}")
    public ResponseEntity<?> returnOrder(@PathVariable("orderId") long orderId,
                                           @PathVariable("cartId") long cartId,
                                           @RequestHeader (name="Authorization") String token) {
        this.currentUserEmail=jwtService.extractUsernameFromToken(token.substring(7));
        return orderService.returnProduct(orderId,cartId,currentUserEmail);
    }

    @PatchMapping("/order/cancel-order/{orderId}/{cartId}")
    public ResponseEntity<?> cancelOrder(@PathVariable("orderId") long orderId,
                                         @PathVariable("cartId") long cartId,
                                         @RequestHeader (name="Authorization") String token){
        this.currentUserEmail=jwtService.extractUsernameFromToken(token.substring(7));
        return orderService.cancelOrder(orderId,cartId,currentUserEmail);
    }

}
