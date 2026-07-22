package com.example.librashare.domain;

import java.time.OffsetDateTime;

import com.example.librashare.controller.BookController;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * 書籍のdomainクラス
 * booksテーブルとの連携
 * 
 * publisher/categrySmallIdの追記
 * ┗仕様変更のため
 * @author furuyama
 * @since 2026-07-08
 * @see BookController
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

    @Column(nullable = false, length = 255)
    private String publisher;

    //カテゴリーのidを取得
    //遅延読み込みの指定-> getCategorySmall() などでアクセスした瞬間にSQLが発行されて取得
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_small_id")
    private CategorySmall categorySmall;

    //作成日時
    //default.nowで設定されているため、insertとupdateの際にはnullが入力される
    //そのためinsertとupdateを実施にデータを取得する際に、findを実施の上、入力された状態で、
    @Column(name = "created_at", nullable = false,  updatable = false, insertable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private boolean deleted = false;
    
    public Book() {
    }

    public Book(Long id, String title, String author, String isbn, String publisher, CategorySmall categorySmall,
            OffsetDateTime createdAt, boolean deleted) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.publisher = publisher;
        this.categorySmall = categorySmall;
        this.createdAt = createdAt;
        this.deleted = deleted;
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

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}


    public String getPublisher() {
        return publisher;
    }


    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public CategorySmall getCategorySmall() {
        return categorySmall;
    }

    public void setCategorySmall(CategorySmall categorySmall) {
        this.categorySmall = categorySmall;
    }

}
