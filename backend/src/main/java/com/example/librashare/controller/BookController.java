package com.example.librashare.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.librashare.domain.Book;
import com.example.librashare.dto.request.BookRequest;
import com.example.librashare.dto.response.BookResponse;
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
    @PreAuthorize("hasAnyRole('general_employee','admin_employee')")
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
    @PreAuthorize("hasAnyRole('general_employee','admin_employee')")
    public ResponseEntity<Book> findById(@PathVariable Long id){
        return bookService.findById(id)
                .map(book -> ResponseEntity.ok(book))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 該当書籍登録API
     * @param BookRequest bookRequest
     * @return　Response 201　作成した書籍の情報を送信
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('admin_employee')")
    public ResponseEntity<BookResponse> createBook(@RequestBody BookRequest bookRequest){
        Long bookId = bookService.createBook(bookRequest);

        BookResponse response = new BookResponse(
                bookId,
                bookRequest.getTitle(),
                bookRequest.getAuthor(),
                bookRequest.getIsbn(),
                bookRequest.getStockCount());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 該当書籍の全更新処理API
     * @param id
     * @param bookRequest
     * @return　成功時：200 存在しない場合：404
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin_employee')")
    public ResponseEntity<BookResponse> updateBook(@PathVariable Long id, @RequestBody BookRequest bookRequest){
        
        Optional <BookResponse> response = bookService.updateBook(id, bookRequest);

        //空チェックを実施
        if (response.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response.get());
    }
}
