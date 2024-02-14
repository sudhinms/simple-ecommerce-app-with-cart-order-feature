package com.ecommerce.app.EcommerceApp.controllers;

import com.ecommerce.app.EcommerceApp.configuration.JwtService;
import com.ecommerce.app.EcommerceApp.dto.productDto.ProductDetailsUserView;
import com.ecommerce.app.EcommerceApp.dto.userDto.*;
import com.ecommerce.app.EcommerceApp.entities.Address;
import com.ecommerce.app.EcommerceApp.enums.Role;
import com.ecommerce.app.EcommerceApp.services.CategoryService;
import com.ecommerce.app.EcommerceApp.services.ProductService;
import com.ecommerce.app.EcommerceApp.services.UserService;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/app")
@Slf4j
public class HomeController {

    @Autowired
    private UserService userService;
    @Autowired
    private ProductService productService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private CategoryService categoryService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@ModelAttribute @Validated UserInfoDto userInfoDto
                                              ,@RequestParam("image")@Nullable MultipartFile image){
        String role= Role.ROLE_USER.name();
        return userService.registerUser(userInfoDto,image,role);
    }
    @PostMapping("/register-admin")
    public ResponseEntity<?> registerAdmin(@ModelAttribute @Validated UserInfoDto userInfoDto
            ,@RequestParam("image")@Nullable MultipartFile image){
        String role= Role.ROLE_ADMIN.name();
        return userService.registerUser(userInfoDto,image,role);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginDto loginDto){
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));
            if (authentication.isAuthenticated()) {
                return new ResponseEntity<>(jwtService.generateToken(loginDto.getEmail()), HttpStatus.CREATED);
            } else {
                throw new UsernameNotFoundException("Not an authorized user!!!!!!");
            }
        }catch (Exception e){
            throw new UsernameNotFoundException("User not found");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader (name="Authorization") String token){
        return userService.logoutUser(token.substring(7));
    }

    @GetMapping("/home/user/profile")
    public ResponseEntity<?> getProfile(@RequestHeader (name="Authorization") String token){
        String email=jwtService.extractUsernameFromToken(token.substring(7));
        return userService.getProfile(email);
    }

    @PutMapping("/home/user/update/password")
    public ResponseEntity<?> updatePassword(@RequestBody @Valid PasswordDto passwordDto,
                                            @RequestHeader (name="Authorization") String token){
        String jwtToken=token.substring(7);
        String email=jwtService.extractUsernameFromToken(jwtToken);
        return userService.updatePassword(email,passwordDto,jwtToken);
    }

    @PatchMapping("/home/user/update/profile")
    public ResponseEntity<?> updateProfile(@Valid @ModelAttribute UpdateProfileDto updateProfileDto,
                                           @RequestParam("image") @Nullable MultipartFile image,
                                           @RequestHeader (name="Authorization") String token){
        String email=jwtService.extractUsernameFromToken(token.substring(7));
        return userService.updateProfile(updateProfileDto,email,image);
    }

    @GetMapping("/home/category/getAll")
    public ResponseEntity<?> getAllCategories(){
        return categoryService.allCategories();
    }

    @GetMapping("/home/product/view-by-category/{categoryId}")
    public ResponseEntity<?> getAllProductsByCategory(@PathVariable("categoryId") long categoryId){
        return productService.viewByCategory(categoryId);
    }

    @GetMapping("/home/product/view-one/{id}")
    public ResponseEntity<ProductDetailsUserView> getSingleProduct(@PathVariable long id){
        return productService.getSingleProductByIdForUsersView(id);
    }

    @GetMapping("/home/product/view/all")
    public ResponseEntity<List<ProductDetailsUserView>> getAllProducts(){
        return productService.getAllProductsForUsersView();
    }
    @GetMapping("/home/user/getAll-address")
    public ResponseEntity<List<Address>> getAllAddresses(@RequestHeader (name="Authorization") String token){
        String currentUserEmail=jwtService.extractUsernameFromToken(token.substring(7));
        return userService.getAllAddress(currentUserEmail);
    }
    @PostMapping("/home/user/create-address")
    public ResponseEntity<?> createAddress(@RequestBody @Valid AddressDto addressDto,
                                           @RequestHeader (name="Authorization") String token){
        String currentUserEmail=jwtService.extractUsernameFromToken(token.substring(7));
        return userService.createNewAddress(currentUserEmail,addressDto);
    }

    @DeleteMapping("/home/user/delete-address/{addressId}")
    public ResponseEntity<?> deleteAddress(@PathVariable("addressId") long addressId){
        return userService.deleteAddressById(addressId);
    }

    @DeleteMapping("/home/user/delete-account")
    public ResponseEntity<?> deleteAccount(@RequestHeader (name="Authorization") String token){
        String currentUserEmail=jwtService.extractUsernameFromToken(token.substring(7));
        return userService.deleteUser(currentUserEmail);
    }
}
