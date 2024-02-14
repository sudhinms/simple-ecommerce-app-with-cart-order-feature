//package com.ecommerce.app.EcommerceApp.configuration;
//
//import com.ecommerce.app.EcommerceApp.entities.Address;
//import com.ecommerce.app.EcommerceApp.entities.OrderPrimaryKey;
//import com.ecommerce.app.EcommerceApp.entities.Orders;
//import com.ecommerce.app.EcommerceApp.exceptions.AddressNotFoundException;
//import com.ecommerce.app.EcommerceApp.exceptions.ProductNotFoundException;
//import com.ecommerce.app.EcommerceApp.repositories.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//import java.sql.Date;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Component
//public class CmdRunner implements CommandLineRunner {
//    @Autowired
//    private OrderRepository orderRepository;
//    @Autowired
//    private CartDetailsRepository cartDetailsRepository;
//    @Autowired
//    private ProductRepository productRepository;
//    @Autowired
//    private UserRepository userRepository;
//    @Autowired
//    private AddressRepository addressRepository;
//
//    @Override
//    public void run(String... args) throws Exception {
//        Orders orders=new Orders();
//
//        orders.setStatus("ORDER_PLACED");
//        orders.setPaymentStatus("PAYED");
//        orders.setQuantity(2);
//        List<Address> address=addressRepository.findByUserInfoId(2L)
//                .orElseThrow(()-> new AddressNotFoundException("addrss not found"));
//        orders.setAddress(address.get(0));
//        orders.setOrderDateTime(LocalDateTime.now());
//        orders.setExpectedDeliveryDate(Date.valueOf(LocalDate.now().plusDays(4)));
//        orders.setTotalPrice(4000);
//        orders.setUserInfo(userRepository.findById(2L).get());
//        orders.setCartDetails(cartDetailsRepository.findById(13L).get());
//        orders.setProductDetails(productRepository.findById(2L).get());
//        OrderPrimaryKey primaryKey=new OrderPrimaryKey();
//        primaryKey.setCartId(13L);
//        primaryKey.setOrderId(2L);
//        orders.setId(primaryKey);
//
//        orderRepository.save(orders);
//    }
//}
