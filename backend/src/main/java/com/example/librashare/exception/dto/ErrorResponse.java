package com.example.librashare.exception.dto;

import com.example.librashare.exception.handler.GlobalExceptionHandler;

/**
 * エラーレスポンス用のdtoクラス
 * @author furuyama
 * @since 2026-07-14
 * @see GlobalExceptionHandler
 */

public class ErrorResponse {

    //機械可読コード（例: VALIDATION_ERROR）
    private String error;
    //人間向け文言（画面表示用）
    private String message;

    
    public ErrorResponse() {
    }

    public ErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    

    

}
