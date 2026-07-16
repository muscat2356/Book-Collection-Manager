package com.example.librashare.domain;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Loanのdomainクラス
 * @author furuyama
 * @since 2026-07-16
 * @see LoanCopy
 * @see LoansController
 */
@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //getBookCopy()を実施するまでDBに取得しない
    //nullにならない
    //DBのカラムのカラムと接続
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_copy_id", nullable = false)
    private BookCopy bookCopy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "borrowed_at", nullable = false)
    private OffsetDateTime borrowedAt;

    @Column(name = "returned_at")
    private OffsetDateTime returnedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private LoanStatus status;

    protected Loan() {
    }

    public Loan(BookCopy bookCopy, User user, OffsetDateTime borrowedAt) {
        this.bookCopy = bookCopy;
        this.user = user;
        this.borrowedAt = borrowedAt;
        this.status = LoanStatus.BORROWED;
    }

    /**
     * 貸し処理メソッド
     * @param bookCopy
     * @param user
     * @param borrowedAt
     * @return
     */
    public static Loan borrow(BookCopy bookCopy, User user, OffsetDateTime borrowedAt){
        bookCopy.markAsLoaned();
        return new Loan(bookCopy, user, borrowedAt);
    }

    /**
     * 返却処理メソッド
     * @param returnedAt
     */
    public void returnBook(OffsetDateTime returnedAt){
        if(this.status == LoanStatus.RETURNED){
            throw new IllegalStateException("既に返却済みです loanId="+id);
        }

        this.status = LoanStatus.RETURNED;
        this.returnedAt = returnedAt;
        this.bookCopy.markAsLoaned();
    }

    /**
     * 返却ステータス更新メソッド
     * @return
     */
    public boolean isReturned(){
        return this.status == LoanStatus.RETURNED;
    }

    public Long getId() {
        return id;
    }

    public BookCopy getBookCopy() {
        return bookCopy;
    }

    public User getUser() {
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
