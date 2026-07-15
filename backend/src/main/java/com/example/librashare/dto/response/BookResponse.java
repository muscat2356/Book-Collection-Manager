package com.example.librashare.dto.response;

import java.util.List;

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
        private int totalCount;
        private int availableCount;
        private List<HoldingResponse> holdings;

        
        public BookResponse() {
        }
        
        public BookResponse(Long id, String title, String author, String isbn,
                            int totalCount, int availableCount,
                            List<HoldingResponse> holdings) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.isbn = isbn;
            this.totalCount = totalCount;
            this.availableCount = availableCount;
            this.holdings = holdings;
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

        
    }
