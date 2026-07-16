package com.example.librashare.dto.response;

import java.time.OffsetDateTime;

/**
 * loans/POST/PUTのレスポンス用DTOクラス
 * @author furuyama
 * @since 2026-07-16
 * @see LoansController
 * @see LoansPostErrorResponse
 */
public class LoansResponse {
    
    private Long id;
    private Long bookCopyId;
    private Long bookId;
    private String bookTitle;
    private Long userId;
    private OffsetDateTime borrowedAt;
    private OffsetDateTime returnedAt;
    private String status;
    
    public LoansResponse() {
    }

    public LoansResponse(Long id, Long bookCopyId, Long bookId, String bookTitle, Long userId,
            OffsetDateTime borrowedAt, OffsetDateTime returnedAt, String status) {
        this.id = id;
        this.bookCopyId = bookCopyId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.userId = userId;
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

    public Long getBookId() {
        return bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public Long getUserId() {
        return userId;
    }

    public OffsetDateTime getBorrowedAt() {
        return borrowedAt;
    }

    public OffsetDateTime getReturnedAt() {
        return returnedAt;
    }

    public String getStatus() {
        return status;
    }

    


}
