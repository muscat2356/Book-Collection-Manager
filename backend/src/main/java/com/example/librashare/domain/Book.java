package com.example.librashare.domain;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 書籍のdomainクラス
 * booksテーブルとの連携
 * @author furuyama
 * @since 2026-07-08
 * @see 
 * BookController
 */

@Entity
@Table(name="books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 255)
    private String author;

    //書籍識別番号
    @Column(length = 32)
    private String isbn;

    //在庫数
    @Column(name = "stock_count", nullable = false)
    private int stockCount;

    //作成日時
    //default.nowで設定されているため、insertとupdateの際にはnullが入力される
    //そのためinsertとupdateを実施にデータを取得する際に、findを実施の上、入力された状態で、
    @Column(name = "created_at", nullable = false,  updatable = false, insertable = false)
    private OffsetDateTime createdAt;

    

    public Book(Long id, String title, String author, String isbn, int stockCount, OffsetDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.stockCount = stockCount;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public int getStockCount() {
        return stockCount;
    }

    public void setStockCount(int stockCount) {
        this.stockCount = stockCount;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }


}
