package com.example.librashare.exception.dto;

import java.util.List;

/**
 * Post/LoansPostのエラーレスポンスのDTOクラス
 * @author furuyama
 * @since 2026-07-16
 * @see LoansController
 */
public class LoansPostErrorResponse {

    private String error;
    private String message;
    //貸し出しできない書籍のリスト
    private List<Long> failedBookCopyIds;

    public LoansPostErrorResponse() {
    }

    public LoansPostErrorResponse(String error, String message, List<Long> failedBookCopyIds) {
        this.error = error;
        this.message = message;
        this.failedBookCopyIds = failedBookCopyIds;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public List<Long> getFailedBookCopyIds() {
        return failedBookCopyIds;
    }

    
    
}
