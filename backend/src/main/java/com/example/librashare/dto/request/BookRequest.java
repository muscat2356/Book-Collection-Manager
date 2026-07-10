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
    private Integer stockCount;

    
    
    public BookRequest(String title, String author, String isbn, Integer stockCount) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.stockCount = stockCount;
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
    public Integer getStockCount() {
        return stockCount;
    }
    public void setStockCount(Integer stockCount) {
        this.stockCount = stockCount;
    }

    


}
