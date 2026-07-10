package com.example.librashare.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.librashare.domain.Book;
import com.example.librashare.service.BookService;

/**
 * 書籍のCRUD機能を実装したRESTController
 * @author furuyama
 * @since 2026-07-08
 * @see 
 * BookService
 * Book
 */

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * 書籍全件取得API
     * @return　本全件のjsonデータをレスポンス
     */
    @GetMapping
    public ResponseEntity<List<Book>> findAll(){
        List<Book> books = bookService.findAll();
        return ResponseEntity.ok(books);
    }

    /**
     * 該当書籍取得API
     * @param id
     * @return　該当書籍のjsonデータ/存在しない場合に404/notfoundをレスポンス
     */
    @GetMapping("/{id}")
    public ResponseEntity<Book> findById(@PathVariable Long id){
        return bookService.findById(id)
                .map(book -> ResponseEntity.ok(book))
                .orElse(ResponseEntity.notFound().build());
    }

}
