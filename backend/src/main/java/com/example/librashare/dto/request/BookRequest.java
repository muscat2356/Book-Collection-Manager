package com.example.librashare.dto.request;

/**
 * 書籍のリクエスト受け入れクラス
 * @author furuyama
 * @since 2026-07-10
 * @see 
 * BookController
 */

public class BookRequest {

    private String title;
    private String author;
    private String isbn;
    private Integer initialCopyCount;

    
    
    public BookRequest(String title, String author, String isbn, Integer initialCopyCount) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
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

}
