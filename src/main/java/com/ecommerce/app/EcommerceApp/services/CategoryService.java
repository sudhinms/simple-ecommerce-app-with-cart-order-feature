package com.ecommerce.app.EcommerceApp.services;

import com.ecommerce.app.EcommerceApp.controllers.AdminController;
import com.ecommerce.app.EcommerceApp.dto.productDto.CategoryDto;
import com.ecommerce.app.EcommerceApp.entities.Categories;
import com.ecommerce.app.EcommerceApp.entities.ProductDetails;
import com.ecommerce.app.EcommerceApp.exceptions.CategoryDetailsException;
import com.ecommerce.app.EcommerceApp.repositories.CategoriesRepository;
import com.ecommerce.app.EcommerceApp.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoriesRepository categoriesRepository;
    @Autowired
    private ProductRepository productRepository;

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Link> createCategory(String categoryName){
        if(categoriesRepository.findByName(categoryName.toLowerCase()).isPresent()){
            throw new CategoryDetailsException("Failed to create, Category already exists..");
        }
        Categories categories=new Categories();
        categories.setName(categoryName.toLowerCase());
        categoriesRepository.save(categories);
        Link link=WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AdminController.class)
                .getAllCategories()).withRel("All_Categories");
        return new ResponseEntity<>(link, HttpStatus.CREATED);
    }

    public ResponseEntity<List<CategoryDto>> allCategories(){
        List<CategoryDto> allCategories=categoriesRepository.findAll().stream()
                .map(categories -> {
                    CategoryDto categoryDto=new CategoryDto();
                    categoryDto.setId(categories.getId());
                    categoryDto.setName(categories.getName());
                    return categoryDto;
                }).toList();
        return new ResponseEntity<>(allCategories,HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteCategory(long categoryId){
        Categories category=categoriesRepository.findById(categoryId)
                .orElseThrow(()->new CategoryDetailsException("category with id "+categoryId+" not found"));
        List<ProductDetails> productDetailsList=category.getProductDetails();
        productDetailsList.forEach(product->{
            product.setCategory(null);
            productRepository.save(product);
        });
        categoriesRepository.delete(category);
        Link link=WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AdminController.class)
                .getAllCategories()).withRel("All_Categories");
        return new ResponseEntity<>(link,HttpStatus.OK);
    }

    public ResponseEntity<List<Categories>> getAllCategoriesWithProducts(){
        return new ResponseEntity<>(categoriesRepository.findAll(),HttpStatus.OK);
    }

}
