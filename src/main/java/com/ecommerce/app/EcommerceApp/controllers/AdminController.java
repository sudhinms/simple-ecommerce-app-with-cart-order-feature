package com.ecommerce.app.EcommerceApp.controllers;

import com.ecommerce.app.EcommerceApp.dto.productDto.OrderDetailDto;
import com.ecommerce.app.EcommerceApp.dto.productDto.ProductDetailsDto;
import com.ecommerce.app.EcommerceApp.dto.productDto.UpdateOrderDetailsDto;
import com.ecommerce.app.EcommerceApp.services.CategoryService;
import com.ecommerce.app.EcommerceApp.services.InvoiceGeneratorService;
import com.ecommerce.app.EcommerceApp.services.OrderService;
import com.ecommerce.app.EcommerceApp.services.ProductService;
import jakarta.annotation.Nullable;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/app/admin")
@MultipartConfig(maxFileSize = 10)
@Slf4j
public class AdminController {

    @Autowired
    private ProductService productService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private InvoiceGeneratorService invoiceGeneratorService;


    @PostMapping("/category/create")
    public ResponseEntity<Link> createCategory(@RequestParam("category")@NotNull String category){
        return categoryService.createCategory(category);
    }

    @DeleteMapping("/category/delete/{categoryId}")
    public ResponseEntity<?> deleteCategoryById(@PathVariable("categoryId") long categoryId){
       return categoryService.deleteCategory(categoryId);
    }

    @PostMapping("/create-new-product")
    public ResponseEntity<?> createProduct(@ModelAttribute @Valid ProductDetailsDto productDetailsDto,
                                           @RequestParam("image") @Nullable MultipartFile image){
        return productService.createNewProduct(productDetailsDto,image);
    }
    @GetMapping("/category/getAll")
    public ResponseEntity<?> getAllCategories(){
        return categoryService.allCategories();
    }
    @PatchMapping("/update-product/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable("id") long id
                                            ,@ModelAttribute @Valid ProductDetailsDto productDetailsDto
                                            ,@RequestParam("image") @Nullable MultipartFile image){
        return productService.updateProductById(id,productDetailsDto,image);
    }

    @DeleteMapping("/delete-product/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable("id") long id){
        return productService.deleteByProductId(id);
    }

    @GetMapping("/admin-view/product/{id}")
    public ResponseEntity<ProductDetailsDto> getSingleProduct(@PathVariable("id") long id){
        return productService.getSingleProductById(id);
    }

    @GetMapping("/admin-view/all-products")
    public ResponseEntity<List<ProductDetailsDto>> getAllProducts(){
        return productService.getAllProducts();
    }

    @GetMapping("/view-all-orders")
    public ResponseEntity<?> getAllOrders(){
        return orderService.getAllUserOrder();
    }

    @PatchMapping("/order/update/{orderId}/{cartId}")
    public ResponseEntity<?> updateOrder(@PathVariable("orderId") long orderId,
                                         @PathVariable("cartId") long cartId,
                                         @RequestBody UpdateOrderDetailsDto orderDetailsDto){
        return orderService.updateOrderDetails(orderId,cartId,orderDetailsDto);
    }
    @GetMapping("/order/{orderId}/{cartId}")
    public ResponseEntity<OrderDetailDto> getOrderDetails(@PathVariable("orderId") long orderId,
                                                          @PathVariable("cartId") long cartId){
        return orderService.getSingleOrder(orderId,cartId);
    }

    @GetMapping("/order/download-sales-report/{startMonth}/{endMonth}")
    public ResponseEntity<byte[]> downloadSalesReport(@RequestHeader (name="Authorization") String token,
                                                      @PathVariable("startMonth") int startMonth,
                                                      @PathVariable("endMonth") int endMonth){
        byte[]invoiceContext=invoiceGeneratorService.salesReportForAdmin(startMonth,endMonth);
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_PDF);
        httpHeaders.setContentDispositionFormData("attachment","invoice.pdf");
        return new ResponseEntity<>(invoiceContext,httpHeaders, HttpStatus.OK);
    }

    @GetMapping("/order/view-sales-report/{startMonth}/{endMonth}")
    public ResponseEntity<String> viewSalesReport(@RequestHeader (name="Authorization") String token,
                                                      @PathVariable("startMonth") int startMonth,
                                                      @PathVariable("endMonth") int endMonth){
        return new ResponseEntity<>(invoiceGeneratorService.getSalesInformation(startMonth,endMonth), HttpStatus.OK);
    }
}
