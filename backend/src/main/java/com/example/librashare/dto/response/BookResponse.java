package com.example.librashare.dto.response;

    /**
     * 書籍のレスポンス受け入れクラス
     * @author furuyama
     * @since 2026-07-10
     * @see 
     * BookController
     */
public class BookResponse {

    private Long id;
    private String title;
    private String author;
    private String isbn;
    private Integer stockCount;
    
    public BookResponse(Long id, String title, String author, String isbn, Integer stockCount) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.stockCount = stockCount;
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
    public Integer getStockCount() {
        return stockCount;
    }
    public void setStockCount(Integer stockCount) {
        this.stockCount = stockCount;
    }





    

}
