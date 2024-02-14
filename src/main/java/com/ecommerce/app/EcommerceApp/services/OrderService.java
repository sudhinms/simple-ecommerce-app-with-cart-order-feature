package com.ecommerce.app.EcommerceApp.services;

import com.ecommerce.app.EcommerceApp.controllers.AdminController;
import com.ecommerce.app.EcommerceApp.controllers.CustomerController;
import com.ecommerce.app.EcommerceApp.dto.paymentsDto.PaymentDto;
import com.ecommerce.app.EcommerceApp.dto.productDto.OrderDetailDto;
import com.ecommerce.app.EcommerceApp.dto.productDto.ProductDetailsDto;
import com.ecommerce.app.EcommerceApp.dto.productDto.UpdateOrderDetailsDto;
import com.ecommerce.app.EcommerceApp.dto.userDto.UserDetailsAdminView;
import com.ecommerce.app.EcommerceApp.entities.*;
import com.ecommerce.app.EcommerceApp.enums.OrderStatus;
import com.ecommerce.app.EcommerceApp.enums.PaymentStatus;
import com.ecommerce.app.EcommerceApp.exceptions.*;
import com.ecommerce.app.EcommerceApp.repositories.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.sql.Date;
import java.util.stream.Collectors;


@Service
@Slf4j
public class OrderService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private CartDetailsRepository cartDetailsRepository;
    private Orders savedOrderForIdDuplication;
    @Autowired
    private OrderIdGeneratingService idGeneratingService;
    @Autowired
    private CartService cartService;


    private long getUserIdWithEmail(String email){
        UserInfo userInfo=userRepository.findByEmail(email)
                .orElseThrow(()->new UsernameNotFoundException("user with email : "+email+" not found"));
        return userInfo.getId();
    }

    private ProductDetails checkQuantity(long productId){
        ProductDetails productDetails=productRepository.findById(productId)
                .orElseThrow(()->new ProductNotFoundException("Product with id : "+productId+" not found"));
        if(productDetails.getQuantity()<=0){
            throw new ProductOutOfStockException("product is out of stock..");
        }
        return productDetails;
    }
    private byte[] getImage(String imagePath){
        if(Files.exists(Path.of(imagePath))){
            try {
                return Files.readAllBytes(Path.of(imagePath));
            } catch (IOException e) {
                return null;
            }
        }
        return new byte[]{};
    }

    private void saveOrderDetails(String email, long productId, int quantity, long cartId){
        UserInfo userInfo=userRepository.findByEmail(email)
                .orElseThrow(()->new UsernameNotFoundException("user with email : "+email+" not found"));
        List<Orders> previousUnfinishedOrders=orderRepository
                .findByUserInfoIdAndPaymentStatus(userInfo.getId(),OrderStatus.ORDER_INITIATED.name());
        previousUnfinishedOrders.addAll(orderRepository
                .findByUserInfoIdAndPaymentStatus(userInfo.getId(),OrderStatus.ORDER_CONFIRMED.name()));
        if(!previousUnfinishedOrders.isEmpty()){
                previousUnfinishedOrders.forEach(order->orderRepository.delete(order));
        }
        ProductDetails productDetails=checkQuantity(productId);
        if(productDetails.getQuantity()<quantity){
            throw new ProductOutOfStockException("Couldn't place order. Only "+productDetails.getQuantity()+" left!!!");
        }

        Orders order=new Orders();
        order.setQuantity(quantity);
        order.setProductDetails(productDetails);
        double totalPrice=(productDetails.getPrice())*quantity;
        order.setTotalPrice(totalPrice);
        order.setUserInfo(userInfo);
        order.setOrderDateTime(LocalDateTime.now());
        Date date=Date.valueOf(order.getOrderDateTime().plusDays(7).toLocalDate());
        order.setExpectedDeliveryDate(date);
        order.setStatus(OrderStatus.ORDER_INITIATED.name());
        order.setPaymentStatus(PaymentStatus.NOT_PAYED.name());

        OrderPrimaryKey primaryKey=new OrderPrimaryKey();
        primaryKey.setCartId(cartId);
        if(this.savedOrderForIdDuplication != null){
            primaryKey.setOrderId(savedOrderForIdDuplication.getOrderKeyId().getOrderId());
        }
        else {
            long newOrderId=idGeneratingService.getNextAvailableId();
            primaryKey.setOrderId(newOrderId);
        }
        order.setOrderKeyId(primaryKey);
        this.savedOrderForIdDuplication=orderRepository.save(order);
    }

    private ResponseEntity<?> initiateOrdering(String email){
        List<Address> addressList = addressRepository.findByUserInfoId(getUserIdWithEmail(email)).get();

        if(addressList.isEmpty()){
            throw new AddressNotFoundException("No address found for your account");
        }
        List<EntityModel<Address>> entityModelList=null;
        CollectionModel<EntityModel<Address>> collectionModel=null;
        entityModelList=addressList.stream()
                   .map(EntityModel::of)
                   .toList();
        collectionModel=CollectionModel.of(entityModelList);
        collectionModel=collectionModel
                .add(Link.of("http://localhost:8081/app/home/order/confirm-address/{addressId}")
                .withRel("Choose_addressID"));
        return ResponseEntity.status(HttpStatus.OK).body(collectionModel);
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<CollectionModel<EntityModel<OrderDetailDto>>> confirmAddress(long addressId, String email) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFoundException("You don't have a valid address..."));
        if (address == null) {
            throw new AddressNotFoundException("You don't have a valid address...");
        }
        List<Orders> orders = orderRepository.findByUserInfoId(getUserIdWithEmail(email));
        List<OrderDetailDto> initiatedOrder = orders
                .stream()
                .filter(order ->
                        Objects.equals(order.getStatus(), "ORDER_INITIATED") && Objects.equals(order.getPaymentStatus(), "NOT_PAYED"))
                .map(order -> {
                    order.setAddress(address);
                    order.setStatus(OrderStatus.ORDER_CONFIRMED.name());
                    Orders savedOrder = orderRepository.save(order);
                    return orderDetailDtoMapper(savedOrder);
                })
                .toList();
        List<EntityModel<OrderDetailDto>> entityModelList = initiatedOrder
                .stream()
                .map(EntityModel::of)
                .collect(Collectors.toList());
        CollectionModel<EntityModel<OrderDetailDto>> collectionModel = CollectionModel.of(entityModelList);
        collectionModel=collectionModel.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CustomerController.class)
                .initiateUpiPayment("",null)).withRel("UPI_Payment"));
        collectionModel=collectionModel.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CustomerController.class)
                .initiateCashOnDelivery("")).withRel("Cash_on_delivery"));
        return new ResponseEntity<>(collectionModel, HttpStatus.OK);
    }

//    @PreAuthorize("hasAuthority('ROLE_USER')")
//    public ResponseEntity<?> orderSingleItem(String email,long productId,int quantity){
//        CartDetails cartDetails=new CartDetails();
//        cartDetails.setQuantity(quantity);
//        cartDetails.setProductId(productId);
//        cartDetails.setUserId(getUserIdWithEmail(email));
//        CartDetails savedCartDetails=cartDetailsRepository.save(cartDetails);
//        saveOrderDetails(email,productId,quantity,savedCartDetails.getId());
//        return initiateOrdering(email);
//    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> checkoutAllProductsInCart(String email) {
        List<CartDetails> allProducts = cartDetailsRepository.findAllByUserInfoId(getUserIdWithEmail(email));
        if (allProducts.isEmpty()) {
            throw new ProductNotFoundException("No items in your cart");
        }
        allProducts.forEach(product->saveOrderDetails(email,product.getProductDetails().getId(),
                product.getQuantity(), product.getId()));
        return initiateOrdering(email);
    }

    private ResponseEntity<?> confirmOrder(String email, List<Orders> allOrders) {
        UserInfo userInfo = userRepository.findByEmail(email)
                .orElseThrow(()->new UsernameNotFoundException("User not found with username : "+email));
        allOrders.forEach(order -> {
            Address address=addressRepository.findById(order.getAddress().getId())
                    .orElseThrow(()->new AddressNotFoundException("You don't have a valid address..."));
            if(userInfo.getId()!=address.getUserInfo().getId()){
                throw new AddressNotFoundException("Invalid address id");
            }
            ProductDetails productDetails=productRepository.findById(order.getProductDetails().getId())
                    .orElseThrow(()->new ProductNotFoundException("product not found"));
            orderRepository.save(order);
            productDetails.setQuantity((productDetails.getQuantity())-(order.getQuantity()));
            productRepository.save(productDetails);
        });
        return getAllOrdersOfUser(email);
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> upiPayment(String email,PaymentDto paymentDto){
        List<Orders> allOrders=orderRepository
                .findByUserInfoIdAndStatus(getUserIdWithEmail(email),"ORDER_CONFIRMED");
        List<Orders> updatedOrders=allOrders.stream().map(order -> {
            order.setStatus(OrderStatus.ORDER_PLACED.name());
            order.setPaymentStatus(PaymentStatus.PAYED.name());
            String paymentId=UUID.randomUUID().toString();
            order.setPaymentNumber(paymentId);
            cartService.deleteFromCart(order.getProductDetails().getId(),email);
            return order;
        }).toList();

        return confirmOrder(email,updatedOrders);
    }

    public ResponseEntity<?> cashOnDeliveryPayment(String email){
        List<Orders> allOrders=orderRepository
                .findByUserInfoIdAndStatus(getUserIdWithEmail(email),"ORDER_CONFIRMED");
        List<Orders> updatedOrders=allOrders.stream().map(order -> {
            order.setStatus(OrderStatus.ORDER_PLACED.name());
            order.setPaymentStatus(PaymentStatus.CASH_ON_DELIVERY.name());
            cartService.deleteFromCart(order.getProductDetails().getId(),email);
            return order;
        }).toList();

        return confirmOrder(email,updatedOrders);
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> getAllOrdersOfUser(String email) {
        UserInfo userInfo=userRepository.findByEmail(email)
                .orElseThrow(()->new UsernameNotFoundException("Users not found with username : "+email));
        List<Orders> ordersList=orderRepository.findByUserInfoId(userInfo.getId())
                .stream()
                .filter(order-> !Objects.equals(order.getStatus(), OrderStatus.ORDER_INITIATED.name())
                        && !Objects.equals(order.getStatus(), OrderStatus.ORDER_CONFIRMED.name()))
                .toList();
        List<OrderDetailDto> orderDetailDtoList=ordersList.stream().map(this::orderDetailDtoMapper).toList();
        List<EntityModel<OrderDetailDto>> entityModelList=orderDetailDtoList
                .stream()
                .map(EntityModel::of)
                .toList();
        if(ordersList.isEmpty()){
            throw new InvalidOrderDetailsException("no order found for user : "+email);
        }
        CollectionModel<EntityModel<OrderDetailDto>> collectionModel=CollectionModel.of(entityModelList);
        Link link=Link.of("http://localhost:8081/app/product/order/{orderId}");
        collectionModel.add(link.withRel("Single_order_details"));
        return new ResponseEntity<>(orderDetailDtoList,HttpStatus.OK);
    }

    private OrderDetailDto orderDetailDtoMapper(Orders order){
        OrderDetailDto orderDetailDto=new OrderDetailDto();
        orderDetailDto.setId(order.getOrderKeyId().getOrderId());
        orderDetailDto.setOrderDateTime(order.getOrderDateTime());
        orderDetailDto.setExpectedDeliveryDate(order.getExpectedDeliveryDate());
        orderDetailDto.setQuantity(order.getQuantity());
        orderDetailDto.setAddress(order.getAddress());
        orderDetailDto.setStatus(order.getStatus());
        orderDetailDto.setPaymentStatus(order.getPaymentStatus());
        orderDetailDto.setCartId(order.getOrderKeyId().getCartId());
        UserInfo userInfo=userRepository.findById(order.getUserInfo().getId())
                .orElseThrow(()->new UsernameNotFoundException("No user found"));
        if (userInfo == null) {
            throw new UsernameNotFoundException("No user found");
        }
        UserDetailsAdminView userDetailsAdminView=new UserDetailsAdminView();
        userDetailsAdminView.setEmail(userInfo.getEmail());
        userDetailsAdminView.setName(userInfo.getName());
        userDetailsAdminView.setMobile(userInfo.getMobile());
        if(userInfo.getProfileImage()!=null){
            userDetailsAdminView.setProfileImage(userInfo.getProfileImage());
        }
        orderDetailDto.setUserDetails(userDetailsAdminView);
        ProductDetails productDetails=productRepository.findById(order.getProductDetails().getId())
                .orElseThrow(()->new ProductNotFoundException("Product not found"));
        ProductDetailsDto productDetailsDto=new ProductDetailsDto();
        productDetailsDto.setId(productDetails.getId());
        productDetailsDto.setName(productDetails.getName());
        productDetailsDto.setBrand(productDetails.getBrand());
        productDetailsDto.setQuantity(order.getQuantity());
        productDetailsDto.setPrice(productDetails.getPrice());
        if(productDetails.getCategory()!=null){
            productDetailsDto.setCategory(productDetails.getCategory().getName());
        }
        if(productDetails.getImagePath()!=null){
            productDetailsDto.setProductImage(getImage(productDetails.getImagePath()));
        }
        orderDetailDto.setProductDetails(productDetailsDto);
        return orderDetailDto;
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getAllUserOrder() {
        List<OrderDetailDto> orderDetailDtoList=orderRepository.findAll()
                .stream()
                .map(this::orderDetailDtoMapper)
                .toList();
        return new ResponseEntity<>(orderDetailDtoList,HttpStatus.OK);
    }

//    @Transactional
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateOrderDetails(long orderId, long cartId, UpdateOrderDetailsDto orderDetailsDto) {
         Orders orders=orderRepository.findByOrderKeyId(new OrderPrimaryKey(orderId,cartId))
                    .orElseThrow(() -> new InvalidOrderDetailsException("Order with id : " + orderId + " not found"));
         try {
             if (orderDetailsDto.getExpectedDeliveryDate() != null) {
                 orders.setExpectedDeliveryDate(orderDetailsDto.getExpectedDeliveryDate());
             }
             if (orderDetailsDto.getStatus() != null) {
                 try {
                     if (orderDetailsDto.getStatus().equals(OrderStatus.RETURNED.name())) {
                         updateProductQuantityWhenProductReturns(orders);
                     }
                     orders.setStatus(OrderStatus.valueOf(orderDetailsDto.getStatus()).name());
                 } catch (IllegalArgumentException e) {
                     throw new IllegalArgumentException("Illegal argument for field 'status'");
                 }
             }

             if (orderDetailsDto.getPaymentStatus() != null) {
                 try {
                     orders.setPaymentStatus(PaymentStatus.valueOf(orderDetailsDto.getPaymentStatus()).name());
                 } catch (IllegalArgumentException e) {
                     throw new IllegalArgumentException("Illegal argument for field 'paymentStatus'");
                 }
             }
             try {
                 orders = orderRepository.save(orders);
             }catch (Exception e){
                 throw new IllegalArgumentException("Illegal details provided for persist");
             }
             OrderDetailDto updatedOrder = orderDetailDtoMapper(orders);
             EntityModel<OrderDetailDto> entityModel = EntityModel.of(updatedOrder);
             entityModel.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AdminController.class)
                     .getAllOrders()).withRel("All_Orders"));
             return new ResponseEntity<>(entityModel, HttpStatus.ACCEPTED);
         }catch (Exception e){
             throw new InvalidOrderDetailsException(e.getMessage());
         }
    }

    private void updateProductQuantityWhenProductReturns(Orders order) {
        long productId=order.getProductDetails().getId();
        int quantity=order.getQuantity();
        ProductDetails productDetails=productRepository.findById(productId)
                .orElseThrow(()->new ProductNotFoundException("product not found"));
        productDetails.setQuantity((productDetails.getQuantity())+quantity);
        productRepository.save(productDetails);
    }

    public ResponseEntity<OrderDetailDto> getSingleOrder(long orderId,long cartId) {
        Orders order=orderRepository.findByOrderKeyId(new OrderPrimaryKey(orderId,cartId))
                .orElseThrow(()->new InvalidOrderDetailsException("No order found with id : "+orderId));
        if(order==null){
            throw new InvalidOrderDetailsException("Order not found with id : "+orderId);
        }

        return new ResponseEntity<>(orderDetailDtoMapper(order),HttpStatus.OK);
    }

    private Orders validateOrder(long orderId,long cartId, String email){
        Orders order=orderRepository.findByOrderKeyId(new OrderPrimaryKey(orderId,cartId))
                .orElseThrow(()->
                        new InvalidOrderDetailsException("Order with order id : "+orderId+" not found"));
        if(!Objects.equals(order.getUserInfo().getEmail(), email)){
            throw new InvalidOrderDetailsException("user '"+email+"' have no order with order id "+orderId);
        }
        return order;
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> returnProduct(long orderId, long cartId, String email) {
        Orders order=validateOrder(orderId,cartId,email);
        Link link=WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CustomerController.class)
                .getAllOrders("")).withRel("All_Orders");

        switch (OrderStatus.valueOf(order.getStatus())){
            case RETURNED:
                throw new ProductDeliveryException("Can't initiate return request because product is already returned",link);
            case RETURN_REQUEST:
                throw new ProductDeliveryException("Can't initiate return request because product is already requested to return",link);
            case DELIVERED:
                order.setStatus(OrderStatus.RETURN_REQUEST.name());
                orderRepository.save(updatePaymentStatus(order));
                updateProductQuantityWhenProductReturns(order);
                return new ResponseEntity<>(link,HttpStatus.OK);
            default:
                throw new ProductDeliveryException("Can't initiate return request. Order status : "+order.getStatus(),link);
        }
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Link> cancelOrder(long orderId, long cartId, String email){
        Orders order=validateOrder(orderId,cartId,email);
        Link defaultLink=WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CustomerController.class)
                .getAllOrders("")).withRel("All_Orders");

        Link returnLink=Link.of("http://localhost:8081/app/home/order/return-order/{orderId}/{cartId}");

        switch (OrderStatus.valueOf(order.getStatus())){
            case DELIVERED:
                throw new ProductDeliveryException("Can't cancel order because product is delivered.You can request to return product",returnLink);
            case OUT_FOR_DELIVERY:
                throw new ProductDeliveryException("Can't cancel order because product is out for delivery.You can request to return product when you get it",returnLink);
            case CANCELLED:
                throw new ProductDeliveryException("Can't cancel order because product is already cancelled",defaultLink);
            case RETURN_REQUEST:
                throw new ProductDeliveryException("Can't initiate cancel request because product is in return state",defaultLink);
            case RETURNED:
                System.out.println("RETURNED");
                throw new ProductDeliveryException("Can't initiate cancel request because product is already returned",defaultLink);
            default:
                order.setStatus(OrderStatus.CANCELLED.name());
                orderRepository.save(updatePaymentStatus(order));
                updateProductQuantityWhenProductReturns(order);
                return new ResponseEntity<>(defaultLink,HttpStatus.OK);
        }
    }

    private Orders updatePaymentStatus(Orders orders){
        if(Objects.equals(orders.getPaymentStatus(), "PAYED")){
            orders.setPaymentStatus(PaymentStatus.REFUND_REQUEST.name());
        }
        return orders;
    }

}
