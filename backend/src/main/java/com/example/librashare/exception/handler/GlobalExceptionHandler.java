package com.example.librashare.exception.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.librashare.exception.dto.ErrorResponse;
import com.example.librashare.exception.exception.BusinessException;
import com.example.librashare.exception.exception.KeycloakOperationException;

/**
 * API例外処理の一元管理クラス
 * 401と403はSpringSecurityで例外処理するため実装しない
 * @author furuyama
 * @since 2026-07-14
 * @see ErrorResponse BusinessException
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final  Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * バリエーションエラーのエラーハンドリングメソッド
     * @return　エラーメッセージ 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validationHandler(){

        ErrorResponse error = new ErrorResponse("VALIDATION_ERROR", "必須項目の入力または形式の違いがあります。");

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * 業務処理のエラーハンドリングメソッド
     * @param e BusinessException(カスタム例外)
     * @return エラーメッセージ、409
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> BusinesHandler(BusinessException e){

        ErrorResponse error = new ErrorResponse(e.getError(), e.getMessage());

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    /**
     * 外部システム連携失敗のエラーハンドリングメソッド
     * @param e
     * @return　エラーメッセージ　500
     */
    @ExceptionHandler(KeycloakOperationException.class)
    public ResponseEntity<ErrorResponse> KeycloakHandler(KeycloakOperationException e){

    ErrorResponse error = new ErrorResponse(e.getError(), e.getMessage());

    return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    //ユーザーが存在しない例外処理-> 404

    /**
     * サーバエラーなどの予期せぬエラーハンドリングメソッド
     * @param e
     * @return エラーメッセージ、500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> ExceptionHandler(Exception e){

        logger.error("予期せぬエラーが発生しました。", e);

        ErrorResponse error = new ErrorResponse("INTERNAL_SERVER_ERROR", "サーバーエラーが発生しています。");

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
