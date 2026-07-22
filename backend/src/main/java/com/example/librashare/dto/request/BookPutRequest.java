package com.example.librashare.dto.request;

import com.example.librashare.controller.BookController;

/**
 * BookのPut用Requestクラス
 * initialCopyCountの変数が存在しないクラス（PUT用）
 * @author furuyama
 * @since 2026-07-22
 * @see BookController
 */
public class BookPutRequest {

    private String title;
    private String author;
    private String isbn;
    private String publisher;
    private Long categorySmallId;
    
    public BookPutRequest() {
    }

    public BookPutRequest(String title, String author, String isbn, String publisher, Long categorySmallId) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.publisher = publisher;
        this.categorySmallId = categorySmallId;
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

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Long getCategorySmallId() {
        return categorySmallId;
    }

    public void setCategorySmallId(Long categorySmallId) {
        this.categorySmallId = categorySmallId;
    }

    
}
