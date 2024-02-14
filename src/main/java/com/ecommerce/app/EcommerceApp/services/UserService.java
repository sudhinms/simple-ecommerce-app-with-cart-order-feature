package com.ecommerce.app.EcommerceApp.services;

import com.ecommerce.app.EcommerceApp.controllers.HomeController;
import com.ecommerce.app.EcommerceApp.dto.userDto.AddressDto;
import com.ecommerce.app.EcommerceApp.dto.userDto.PasswordDto;
import com.ecommerce.app.EcommerceApp.dto.userDto.UpdateProfileDto;
import com.ecommerce.app.EcommerceApp.dto.userDto.UserInfoDto;
import com.ecommerce.app.EcommerceApp.entities.Address;
import com.ecommerce.app.EcommerceApp.entities.ExpiredToken;
import com.ecommerce.app.EcommerceApp.entities.Orders;
import com.ecommerce.app.EcommerceApp.entities.UserInfo;
import com.ecommerce.app.EcommerceApp.exceptions.AddressNotFoundException;
import com.ecommerce.app.EcommerceApp.exceptions.FileReadWriteException;
import com.ecommerce.app.EcommerceApp.exceptions.PasswordNotMatchException;
import com.ecommerce.app.EcommerceApp.repositories.*;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


@Service
@Slf4j
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private ExpiredTokenRepository tokenRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CartDetailsRepository cartDetailsRepository;

    public ResponseEntity<?> registerUser(UserInfoDto detailsDto, MultipartFile image,String role){
        if(isUserAlreadyPresent(detailsDto.getEmail())){
            return new ResponseEntity<>("User already present. Try login",HttpStatus.ACCEPTED);
        }
        UserInfo userInfo=UserInfo.builder()
                .email(detailsDto.getEmail())
                .name(detailsDto.getName())
                .password(passwordEncoder.encode(detailsDto.getPassword()))
                .mobile(detailsDto.getMobile())
                .role(role)
                .build();
        ResponseEntity<Object> response=validateAndSetImage(image,userInfo);
        UserInfoDto dto= (UserInfoDto) response.getBody();
        assert dto != null;
        EntityModel<UserInfoDto> entityModel=EntityModel.of(dto);
        entityModel.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(HomeController.class)
                .login(null)).withRel("Login"));
        return new ResponseEntity<>(entityModel,HttpStatus.CREATED);
    }

    private ResponseEntity<Object> validateAndSetImage(MultipartFile image,UserInfo userInfo){
        if(image!=null){
            try {
                if (!Objects.requireNonNull(image.getContentType()).startsWith("image/")) {
                    return ResponseEntity.badRequest().body("Only images are allowed");
                }
                userInfo.setProfileImage(image.getBytes());
            }catch (Exception e){
                throw new FileReadWriteException("Cant write image data to db");
            }
        }
        try {
            UserInfo userInfo1=userRepository.save(userInfo);
            UserInfoDto userInfoDto=new UserInfoDto();
            userInfoDto.setName(userInfo1.getName());
            userInfoDto.setEmail(userInfo1.getEmail());
            userInfoDto.setMobile(userInfo1.getMobile());
            userInfoDto.setPassword("*************");
            if(userInfo1.getProfileImage()!=null){
                userInfoDto.setProfileImage(userInfo1.getProfileImage());
            }
            return new ResponseEntity<>(userInfoDto,HttpStatus.CREATED);
        }catch (Exception e){
            throw new ValidationException("Invalid details");
        }

    }

    private boolean isUserAlreadyPresent(String email){
        return userRepository.findByEmail(email).isPresent();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<?> getProfile(String email) {
        UserInfo userInfo=userRepository.findByEmail(email).get();
        UserInfoDto userInfoDto = UserInfoDto.builder()
                .email(userInfo.getEmail())
                .name(userInfo.getName())
                .mobile(userInfo.getMobile())
                .password("**********")
                .build();
        if(userInfo.getProfileImage()!=null){
            userInfoDto.setProfileImage(userInfo.getProfileImage());
        }
        return new ResponseEntity<>(userInfoDto,HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<?> updatePassword(String email, PasswordDto passwordDto,String token) {
        UserInfo userInfo=userRepository.findByEmail(email).get();
        if(!passwordEncoder.matches(passwordDto.getOldPassword(), userInfo.getPassword())){
            throw new PasswordNotMatchException("Incorrect password for user : "+email);
        }
        if(!Objects.equals(passwordDto.getNewPassword(), passwordDto.getConfirmPassword())){
            throw new PasswordNotMatchException("Confirm password not match with new password");
        }
        userInfo.setPassword(passwordEncoder.encode(passwordDto.getNewPassword()));
        userRepository.save(userInfo);
        ExpiredToken expiredToken=new ExpiredToken();
        expiredToken.setLogoutTime(LocalDateTime.now());
        expiredToken.setToken(token);
        tokenRepository.save(expiredToken);
        Link loginLink=WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(HomeController.class)
                .login(null)).withRel("Login");
        return new ResponseEntity<>(loginLink,HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<?> updateProfile(UpdateProfileDto updateProfileDto, String email, MultipartFile image) {
        try{
            UserInfo userInfo=userRepository.findByEmail(email).get();
            if(updateProfileDto.getName()!=null){
                userInfo.setName(updateProfileDto.getName());
            }
            if(updateProfileDto.getMobile()!=null){
                userInfo.setMobile(updateProfileDto.getMobile());
            }
            if(updateProfileDto.getEmail()!=null){
                userInfo.setEmail(updateProfileDto.getEmail());
            }
            return validateAndSetImage(image,userInfo);
        }catch (Exception e){
            throw new ValidationException("Invalid method arguments");
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<List<Address>> getAllAddress(String currentUserEmail) {
        UserInfo userInfo=userRepository.findByEmail(currentUserEmail)
                .orElseThrow(()->new UsernameNotFoundException("Invalid username"));
        List<Address> addressList=addressRepository.findByUserInfoId(userInfo.getId())
                .orElseThrow(()->new AddressNotFoundException("You don't have any address associated with your account"));
        if(addressList.isEmpty()){
            throw new AddressNotFoundException("You don't have any address associated with your account");
        }
        return new ResponseEntity<>(addressList,HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<Link> createNewAddress(String email, AddressDto addressDto){
        UserInfo userInfo=userRepository.findByEmail(email)
                .orElseThrow(()->new UsernameNotFoundException("Users not found with username : "+email));
        Address address= new Address();
        address.setCity(addressDto.getCity());
        address.setPin(addressDto.getPin());
        address.setState(addressDto.getState());
        address.setStreet(addressDto.getStreet());
        address.setUserInfo(userInfo);
        addressRepository.save(address);
        Link link= WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(HomeController.class)
                .getAllAddresses(null)).withRel("Get_All_Addresses");
        return new ResponseEntity<>(link,HttpStatus.CREATED);
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<?> deleteUser(String currentUserEmail) {
        UserInfo userInfo=userRepository.findByEmail(currentUserEmail)
                .orElseThrow(()->new UsernameNotFoundException("Username not found"));
        orderRepository.deleteByUserInfoId(userInfo.getId());
        addressRepository.deleteByUserInfoId(userInfo.getId());
        cartDetailsRepository.deleteByUserInfoId(userInfo.getId());
        userRepository.deleteByEmail(currentUserEmail);
        return ResponseEntity.status(HttpStatus.OK).body("Account deleted successfully");
    }

    public ResponseEntity<?> logoutUser(String token){
        ExpiredToken expiredToken=new ExpiredToken();
        expiredToken.setLogoutTime(LocalDateTime.now());
        expiredToken.setToken(token);
        tokenRepository.save(expiredToken);
        Link loginLink=WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(HomeController.class)
                .login(null)).withRel("Login");
        return new ResponseEntity<>(loginLink,HttpStatus.OK);
    }

    public ResponseEntity<?> deleteAddressById(long addressId) {
        addressRepository.findById(addressId)
                .orElseThrow(()-> new AddressNotFoundException("Address with id "+addressId+" not found"));
        List<Orders> ordersList=orderRepository.findByAddressId(addressId);
        if(!ordersList.isEmpty()){
            ordersList.forEach(order->{
                order.setAddress(null);
                orderRepository.save(order);
            });
        }
        addressRepository.deleteById(addressId);
        Link link = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(HomeController.class)
                .getAllAddresses(null)).withRel("All_Address");
        return ResponseEntity.status(HttpStatus.OK).body(link);
    }
}
