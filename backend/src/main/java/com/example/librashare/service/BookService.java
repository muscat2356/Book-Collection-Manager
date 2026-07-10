package com.example.librashare.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.librashare.domain.Book;
import com.example.librashare.dto.request.BookRequest;
import com.example.librashare.repository.BookRepository;

import jakarta.transaction.Transactional;

/**
 * 書籍のCRUD機能を実装したService
 * @author furuyama
 * @since 2026-07-08
 * @see 
 * BookRepository
 */
@Service
@Transactional
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /**
     * 書籍の全件検索
     * @return　DBから書籍を全件リターン
     */
    public List<Book> findAll() {
       return bookRepository.findAll();
    }

    /**
     * 該当書籍の検索
     * @param id
     * @return　DBから該当書籍IDをリターン（レスポンスで使用するため）
     */
    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }

    /**
     * 該当書籍登録Service
     * @param BookRequest bookRequest　該当書籍の情報
     * @return　Response 201　作成した書籍の情報を送信
     */
    public Long createBook(BookRequest request) {
        Book book = new Book(null, request.getTitle(), request.getAuthor(), request.getIsbn(), request.getStockCount(), null);
        Book saved = bookRepository.save(book);
        return saved.getId();
    }
}
