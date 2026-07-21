package com.example.librashare.domain;

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
 * BookCopyのdomainクラス
 * @author furuyama
 * @since 2026-07-16
 * @see LoansController
 */
@Entity
@Table(name = "book_copies")
public class BookCopy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CopyStatus status;

    @Column(nullable = false)
    private boolean deleted = false;

    public BookCopy() {
    }

    public BookCopy(Book book) {
        this.book = book;
        this.status = CopyStatus.AVAILABLE;
    }

    void markAsLoaned(){
        if (this.status != CopyStatus.AVAILABLE) {
            throw new IllegalStateException("貸し出しできない状態です copuID:"+ id);
        }
        this.status = CopyStatus.LOANED;
    }

    void markAsAvailable(){
        this.status = CopyStatus.AVAILABLE;
    }

    public boolean isAvailable(){
        return this.status == CopyStatus.AVAILABLE;
    }

    public Long getId() {
        return id;
    }

    public Book getBook() {
        return book;
    }

    public CopyStatus getStatus() {
        return status;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    
    
}
