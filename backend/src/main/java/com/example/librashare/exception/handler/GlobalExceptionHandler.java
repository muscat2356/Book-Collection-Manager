package com.example.librashare.exception.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.example.librashare.exception.dto.ErrorResponse;
import com.example.librashare.exception.exception.BussinessException;

//401と403はSpringSecurityで例外処理するため実装しない

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final  Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    //400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validationHandler(){

        ErrorResponse error = new ErrorResponse("VALIDATION_ERROR", "必須項目の入力または形式の違いがあります。");

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    //409
    @ExceptionHandler(BussinessException.class)
    public ResponseEntity<ErrorResponse> BussinesHandler(BussinessException e){

        ErrorResponse error = new ErrorResponse(e.getError(), e.getMessage());

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    //500エラー
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> ExceptionHandler(Exception e){

        logger.error("予期せぬエラーが発生しました。", e);

        ErrorResponse error = new ErrorResponse("INTERNAL SERVER ERROR", "サーバーエラーが発生しています。");

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
