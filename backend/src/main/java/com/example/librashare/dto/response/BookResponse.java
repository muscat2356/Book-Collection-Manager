package com.example.librashare.dto.response;

import java.util.List;

import com.example.librashare.controller.BookController;

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
        //追加フィールド変数
        private String publisher;
        private int totalCount;
        private int availableCount;
        private List<HoldingResponse> holdings;
        //追加フィールド変数
        private CategoryResponse categoryResponse;

        
        public BookResponse() {
        }

        public BookResponse(Long id, String title, String author, String isbn, String publisher, int totalCount,
                int availableCount, List<HoldingResponse> holdings, CategoryResponse categoryResponse) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.isbn = isbn;
            this.publisher = publisher;
            this.totalCount = totalCount;
            this.availableCount = availableCount;
            this.holdings = holdings;
            this.categoryResponse = categoryResponse;
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
        public int getTotalCount() {
            return totalCount;
        }
        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }
        public int getAvailableCount() {
            return availableCount;
        }
        public void setAvailableCount(int availableCount) {
            this.availableCount = availableCount;
        }
        public List<HoldingResponse> getHoldings() {
            return holdings;
        }
        public void setHoldings(List<HoldingResponse> holdings) {
            this.holdings = holdings;
        }

        public CategoryResponse getCategoryResponse() {
            return categoryResponse;
        }

        public void setCategoryResponse(CategoryResponse categoryResponse) {
            this.categoryResponse = categoryResponse;
        }

        public String getPublisher() {
            return publisher;
        }

        public void setPublisher(String publisher) {
            this.publisher = publisher;
        }
        
    }
