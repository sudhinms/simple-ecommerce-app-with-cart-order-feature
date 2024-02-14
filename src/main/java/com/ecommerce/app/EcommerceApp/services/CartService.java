package com.ecommerce.app.EcommerceApp.services;

import com.ecommerce.app.EcommerceApp.dto.productDto.CartDto;
import com.ecommerce.app.EcommerceApp.entities.CartDetails;
import com.ecommerce.app.EcommerceApp.entities.ProductDetails;
import com.ecommerce.app.EcommerceApp.entities.UserInfo;
import com.ecommerce.app.EcommerceApp.exceptions.FileReadWriteException;
import com.ecommerce.app.EcommerceApp.exceptions.ProductNotFoundException;
import com.ecommerce.app.EcommerceApp.exceptions.ProductOutOfStockException;
import com.ecommerce.app.EcommerceApp.repositories.CartDetailsRepository;
import com.ecommerce.app.EcommerceApp.repositories.ProductRepository;
import com.ecommerce.app.EcommerceApp.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
@Slf4j
public class CartService {

    @Autowired
    private ProductService productService;
    @Autowired
    private CartDetailsRepository cartDetailsRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;

    private long getUserIdWithEmail(String email){
        return userRepository.findByEmail(email).get().getId();
    }
    private byte[] getImage(String imagePath){
        if(Files.exists(Path.of(imagePath))){
            try {
                return Files.readAllBytes(Path.of(imagePath));
            } catch (IOException e) {
                throw new FileReadWriteException(e.getMessage()+"\nCan't read image from : "+imagePath);
            }
        }
        return new byte[]{};
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> addToCart(long id,String email,int quantity) {
        ProductDetails product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Requested product with id : " + id + " not found"));
        try {
            if (product == null) {
                throw new ProductNotFoundException("Requested product with id : " + id + " not found");
            }
            if (product.getQuantity() < 1) {
                throw new ProductOutOfStockException("Product is out of stock");
            }
            if (product.getQuantity() < quantity) {
                throw new ProductOutOfStockException("only " + product.getQuantity() + " left");
            }
            List<CartDetails> cartDetailsList = cartDetailsRepository.findAllByUserInfoId(getUserIdWithEmail(email));
            List<CartDetails> existingWithSameId = cartDetailsList.stream()
                    .filter(cartItem -> cartItem.getProductDetails().getId() == id).toList();
            if (!existingWithSameId.isEmpty()) {
                return new ResponseEntity<>("Item already present in cart..", HttpStatus.FOUND);
            }
            UserInfo userInfo=userRepository.findByEmail(email)
                    .orElseThrow(()->new UsernameNotFoundException("user not found"));
            CartDetails cartDetails = new CartDetails();
            cartDetails.setUserInfo(userInfo);
            cartDetails.setProductDetails(product);
            cartDetails.setQuantity(quantity);
            cartDetailsRepository.save(cartDetails);

            CartDto cartDto = CartDto.builder()
                    .productName(product.getName())
                    .productId(product.getId())
                    .price(product.getPrice())
                    .quantity(quantity)
                    .build();
            if (product.getImagePath() != null) {
                cartDto.setImage(getImage(product.getImagePath()));
            }
            return new ResponseEntity<>(cartDto, HttpStatus.OK);
        }catch (Exception e){
            throw new ProductOutOfStockException(e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<List<CartDto>> getAllItemsInCart(String email){
            List<CartDetails> cartDetailsList = cartDetailsRepository.findAllByUserInfoId(getUserIdWithEmail(email));
            List<CartDto> allItems=cartDetailsList.stream()
                    .map(cart->{
                        ProductDetails product=null;
                        try {
                            product = cart.getProductDetails();
                        }catch (Exception e){
                            throw new ProductNotFoundException("product with id is not present");
                        }
                        CartDto cartDto=CartDto.builder()
                                .quantity(cart.getQuantity())
                                .productName(product.getName())
                                .price(product.getPrice())
                                .productId(product.getId())
                                .build();
                        if (product.getImagePath() != null) {
                            cartDto.setImage(getImage(product.getImagePath()));
                        }
                        return cartDto;
                    })
                    .toList();
            if (allItems.isEmpty()) {
                throw new ProductNotFoundException("No items in the cart");
            }
            return new ResponseEntity<>(allItems, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> deleteFromCart(long productId,String email){
        if(cartDetailsRepository.findByProductDetailsIdAndUserInfoId(productId,getUserIdWithEmail(email))==null){
            throw new ProductNotFoundException("Product with id : "+productId+"  not found");
        }
        cartDetailsRepository.deleteByUserInfoIdAndProductDetailsId(getUserIdWithEmail(email),productId);
        Link link=Link.of("http://localhost:8081/app/home/cart/getAll")
                .withRel("Get_All_Cart_Items");
        return ResponseEntity.status(HttpStatus.OK).body(link);
    }
}
