package com.ecommerce.app.EcommerceApp.exceptions;


public class FileReadWriteException extends RuntimeException{
    public FileReadWriteException(String message){
        super(message);
    }
}
