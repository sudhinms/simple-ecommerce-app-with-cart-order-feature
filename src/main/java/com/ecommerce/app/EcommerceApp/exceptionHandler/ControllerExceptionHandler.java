package com.ecommerce.app.EcommerceApp.exceptionHandler;

import com.ecommerce.app.EcommerceApp.controllers.HomeController;
import com.ecommerce.app.EcommerceApp.exceptions.*;
import com.ecommerce.app.EcommerceApp.exceptions.ProductDeliveryException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.IOException;
import java.nio.file.AccessDeniedException;


@RestControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @Nullable
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail problemDetail=ProblemDetail.forStatus(HttpStatusCode.valueOf(status.value()));
        problemDetail.setProperty("Invalid_Argument","Argument format is incorrect");
        problemDetail.setTitle("MethodArgumentNotValidException");
        problemDetail.setDetail(ex.getMessage());
        return new ResponseEntity<>(problemDetail, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PasswordNotMatchException.class)
    public ProblemDetail handlePasswordNotValid(Exception e){
        ProblemDetail problemDetail=ProblemDetail.forStatus(HttpStatusCode.valueOf(422));
        problemDetail.setDetail(e.getMessage());
        problemDetail.setTitle("PasswordNotMatchException");
        problemDetail.setProperty("Invalid_password","Password not match required criteria");
        return problemDetail;
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ProblemDetail handleProductNotFound(Exception e){
        ProblemDetail problemDetail=ProblemDetail.forStatus(HttpStatusCode.valueOf(404));
        problemDetail.setDetail(e.getMessage());
        problemDetail.setTitle("ProductNotFoundException");
        problemDetail.setProperty("Data_Not_Found","Product not found");
        Link link= WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(HomeController.class)
                .getAllProducts()).withRel("All_products");
        problemDetail.setType(link.toUri());
        return problemDetail;
    }
    @ExceptionHandler(ProductOutOfStockException.class)
    public ProblemDetail handleProductOutOfStockException(Exception e){
        ProblemDetail problemDetail=ProblemDetail.forStatus(HttpStatusCode.valueOf(404));
        problemDetail.setDetail(e.getMessage());
        problemDetail.setTitle("ProductOutOfStockException");
        problemDetail.setProperty("Data_Not_Found","Product out of stock");
        Link link= WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(HomeController.class)
                .getAllProducts()).withRel("All_products");
        problemDetail.setType(link.toUri());
        return problemDetail;
    }

    @ExceptionHandler(InvalidOrderDetailsException.class)
    public ProblemDetail handleInvalidOrderDetailsException(Exception e){
        ProblemDetail problemDetail=ProblemDetail.forStatus(HttpStatusCode.valueOf(404));
        problemDetail.setDetail(e.getMessage());
        problemDetail.setTitle("InvalidOrderDetailsException");
        problemDetail.setProperty("Order_Details_Exception","Invalid order details");
        Link link= WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(HomeController.class)
                .getAllProducts()).withRel("All_products");
        problemDetail.setType(link.toUri());
        return problemDetail;
    }

    @ExceptionHandler(FileReadWriteException.class)
    public ProblemDetail handleFileUpload(Exception e){
        ProblemDetail problemDetail=ProblemDetail.forStatus(HttpStatusCode.valueOf(404));
        problemDetail.setDetail(e.getMessage());
        problemDetail.setTitle("FileReadWriteException");
        problemDetail.setProperty("File_Upload_Error","error while uploading file");
        return problemDetail;
    }

    @ExceptionHandler(AddressNotFoundException.class)
    public ProblemDetail handleAddressNotFoundException(Exception e){
        ProblemDetail problemDetail=ProblemDetail.forStatus(HttpStatusCode.valueOf(404));
        problemDetail.setDetail(e.getMessage());
        problemDetail.setTitle("AddressNotFoundException");
        problemDetail.setProperty("Address_Error","Not found any address for your account");
        Link link=Link.of("http://localhost:8081/app/home/user/create-address","Create_Address");
        problemDetail.setProperty("Add_Address",link);
        return problemDetail;
    }
    @ExceptionHandler(MalformedJwtException.class)
    public ProblemDetail handleMalformedJwtException(Exception e){
        ProblemDetail problemDetail=ProblemDetail.forStatus(HttpStatusCode.valueOf(404));
        problemDetail.setDetail(e.getMessage());
        problemDetail.setTitle("MalformedJwtException");
        problemDetail.setProperty("Access_denied","Jwt token contains invalid chars or white space");
        return problemDetail;
    }

    @ExceptionHandler(RuntimeException.class)
    public ProblemDetail handleRuntimeException(Exception e) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatusCode.valueOf(404));
        problemDetail.setDetail(e.getMessage());
        problemDetail.setTitle(e.getClass().toString());
//        problemDetail.setProperty("Access_denied","Jwt token contains invalid chars or white space");
        return problemDetail;
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ProblemDetail handleUsernameNotFound(Exception e) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatusCode.valueOf(403));
        problemDetail.setDetail(e.getMessage());
        problemDetail.setTitle("UsernameNotFoundException");
        problemDetail.setProperty("Access_Denied", "Invalid username");
        Link link= WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(HomeController.class)
                .getProfile("")).withRel("User_profile");
        problemDetail.setType(link.toUri());
        return problemDetail;
    }
    @ExceptionHandler(ProductDeliveryException.class)
    public ProblemDetail handleProductDeliveryException(Exception e, HttpServletRequest request) throws IOException {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatusCode.valueOf(403));
        problemDetail.setDetail(e.getMessage());
        problemDetail.setTitle("ProductDeliveryException");
        problemDetail.setProperty("Order_Status", "Exception in changing order status");
        problemDetail.setType(ProductDeliveryException.getLink().toUri());
        return problemDetail;
    }
    @ExceptionHandler(CategoryDetailsException.class)
    public ProblemDetail handleCategoryDetailsException(Exception e) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatusCode.valueOf(403));
        problemDetail.setDetail(e.getMessage());
        problemDetail.setTitle("CategoryDetailsException");
        problemDetail.setProperty("Category_Exception", "Exception in category details");
        return problemDetail;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDeniedException(Exception e) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatusCode.valueOf(401));
        problemDetail.setDetail(e.getMessage());
        problemDetail.setTitle("AccessDeniedException");
        problemDetail.setProperty("AccessDeniedException", "you are not authorized to hit this url");
        problemDetail.setType((WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(HomeController.class)
                .login(null)).withRel("Login")).toUri());
        return problemDetail;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(Exception e) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatusCode.valueOf(422));
        problemDetail.setDetail(e.getMessage());
        problemDetail.setTitle("IllegalArgumentException");
        problemDetail.setProperty("Illegal_Argument_Exception", "Invalid argument details");
        return problemDetail;
    }
}
