package com.example.librashare.dto.request;

import com.example.librashare.controller.BookController;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 書籍のリクエスト受け入れクラス
 * @author furuyama
 * @since 2026-07-10
 * @see 
 * BookController
 */

public class BookRequest {

    @NotBlank
    @Size(max = 255)
    private String title;

    @NotBlank
    @Size(max = 255)
    private String author;

    @Size(max = 32)
    private String isbn;

    @NotBlank
    @Size(max = 255)
    private String publisher;

    private Long categorySmallId;

    private Integer initialCopyCount;

    public BookRequest() {
    }

    public BookRequest(String title, String author, String isbn, String publisher, Long categorySmallId,
            Integer initialCopyCount) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.publisher = publisher;
        this.categorySmallId = categorySmallId;
        this.initialCopyCount = initialCopyCount;
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

    public Integer getInitialCopyCount() {
        return initialCopyCount;
    }

    public void setInitialCopyCount(Integer initialCopyCount) {
        this.initialCopyCount = initialCopyCount;
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
