package com.example.librashare.dto.response;

/**
 * LoansDetailsResponseにネストしてレスポンスするBookDTOクラス
 * @author furuyama
 * @since 2026-07-16
 * @see LoansDetailsResponse
 */
public class BookSummary {

    private Long id;
    private String title;
    private String author;

    public BookSummary() {
    }

    public BookSummary(Long id, String title, String author) {
        this.id = id;
        this.title = title;
        this.author = author;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    
    

}
