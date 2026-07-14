package com.example.librashare.exception.exception;

public class BussinessException extends RuntimeException {


    private final String error;

    public BusinessException(String error, String message) {
        super(message);
        this.error = error;
    }

    public String getError() {
        return error;
    }

    
    

}
