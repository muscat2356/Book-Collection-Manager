package com.example.librashare.dto.response;

import java.util.List;

/**
 * LoansPostResponseをラップするListのDTOレスポンスクラス
 *@author furuyama
 *@since 2026-07-16
 *@see LoansController
 */
public class LoansPostListResponse {

    //貸出書籍のリスト
    private List<LoansPostResponse> loans;

    public LoansPostListResponse() {
    }

    public LoansPostListResponse(List<LoansPostResponse> loans) {
        this.loans = loans;
    }

    public List<LoansPostResponse> getLoans() {
        return loans;
    }

}
