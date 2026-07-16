package com.example.librashare.dto.response;

import java.time.OffsetDateTime;

import com.example.librashare.domain.LoanStatus;

/**
 * GET/貸出情報と紐づけてUser/BookデータをレスポンスするDTOクラス
 * @author furuyama
 * @since 2026-07-16
 * @see BookSummary
 * @see UserSummary
 * @see LoansController
 */
public class LoansDetailsResponse {

    private Long id;
    private Long bookCopyId;
    //書籍情報（ネストして返す）
    private BookSummary book;
    //利用者情報（ネストして返す）
    private UserSummary user;
    private OffsetDateTime borrowedAt;
    private OffsetDateTime returnedAt;
    private LoanStatus status;

    public LoansDetailsResponse() {
    }

    

    public LoansDetailsResponse(Long id, Long bookCopyId, BookSummary book, UserSummary user, OffsetDateTime borrowedAt,
            OffsetDateTime returnedAt, LoanStatus status) {
        this.id = id;
        this.bookCopyId = bookCopyId;
        this.book = book;
        this.user = user;
        this.borrowedAt = borrowedAt;
        this.returnedAt = returnedAt;
        this.status = status;
    }



    public Long getId() {
        return id;
    }

    public Long getBookCopyId() {
        return bookCopyId;
    }

    public BookSummary getBook() {
        return book;
    }

    public UserSummary getUser() {
        return user;
    }

    public OffsetDateTime getBorrowedAt() {
        return borrowedAt;
    }

    public OffsetDateTime getReturnedAt() {
        return returnedAt;
    }

    public LoanStatus getStatus() {
        return status;
    }
    
}
