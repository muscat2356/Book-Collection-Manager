package com.example.librashare.dto.request;

import java.util.List;

/**
 * 貸出のリクエスト用DTOクラス
 * @author furuyama
 * @since 2026-07-16
 * @see LoansController
 */
public class LoansRequest {

    private Long userId;
    //貸出書籍のIDリスト
    private List<Long> bookCopyIds;

    
    public LoansRequest() {
    }


    public LoansRequest(Long userId, List<Long> bookCopyIds) {
        this.userId = userId;
        this.bookCopyIds = bookCopyIds;
    }


    public Long getUserId() {
        return userId;
    }


    public void setUserId(Long userId) {
        this.userId = userId;
    }


    public List<Long> getBookCopyIds() {
        return bookCopyIds;
    }


    public void setBookCopyIds(List<Long> bookCopyIds) {
        this.bookCopyIds = bookCopyIds;
    }

    

}
