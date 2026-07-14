package com.example.librashare.exception.exception;

import com.example.librashare.exception.handler.GlobalExceptionHandler;

/**
 * 業務処理でのエラー発生時の例外クラス（ステータスコード:409）
 * @author furuyama
 * @since 2026-07-14
 * @see GlobalExceptionHandler
 * BusinessException
 */

public class BusinessException extends RuntimeException {

    //機械可読コード
    private final String error;

    /*
    businessExceptionのコード例
    下記の場合に上記のカスタム例外を発生させる

    USER_HAS_ACTIVE_LOANS	
    INSUFFICIENT_STOCK
    LOAN_ALREADY_RETURNED
    */

    public BusinessException(String error, String message) {
        super(message);
        this.error = error;
    }

    public String getError() {
        return error;
    }

    
    

}
