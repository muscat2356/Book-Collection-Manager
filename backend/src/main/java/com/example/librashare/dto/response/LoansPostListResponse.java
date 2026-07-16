package com.example.librashare.dto.response;

import java.util.List;

/**
 * LoansPostResponseをラップするListのDTOレスポンスクラス
 *@author furuyama
 *@since 2026-07-16
 *@see LoansController
 *@see LoansPostResponse 
 */
public class LoansPostListResponse {

    //貸出書籍のリスト
    private List<LoansResponse> loans;

    public LoansPostListResponse() {
    }

    public LoansPostListResponse(List<LoansResponse> loans) {
        this.loans = loans;
    }

    public List<LoansResponse> getLoans() {
        return loans;
    }

}
