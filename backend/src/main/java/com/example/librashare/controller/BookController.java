package com.example.librashare.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.librashare.domain.BookCopy;
import com.example.librashare.dto.request.BookPutRequest;
import com.example.librashare.dto.request.BookRequest;
import com.example.librashare.dto.response.BookResponse;
import com.example.librashare.dto.response.CopiesResponse;
import com.example.librashare.service.BookService;

import jakarta.validation.Valid;

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
    public ResponseEntity<List<BookResponse>> findAll(){
        List<BookResponse> books = bookService.findAll();
        return ResponseEntity.ok(books);
    }

    /**
     * 該当書籍取得API
     * @param id
     * @return　該当書籍のjsonデータ/存在しない場合に404/notfoundをレスポンス
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('general_employee','admin_employee')")
    public ResponseEntity<BookResponse> findById(@PathVariable Long id){
        return bookService.findById(id)
            .map(b -> ResponseEntity.ok(b))
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 該当書籍登録API
     * @param BookRequest bookRequest
     * @return　Response 201　作成した書籍の情報を送信
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('admin_employee')")
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookRequest bookRequest){
        BookResponse response = bookService.createBook(bookRequest);

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
    public ResponseEntity<BookResponse> updateBook(@PathVariable Long id,@Valid @RequestBody BookPutRequest bookRequest){
        
        Optional <BookResponse> response = bookService.updateBook(id, bookRequest);

        //空チェックを実施
        if (response.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response.get());
    }

    /**
     * 該当書籍の削除処理API
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin_employee')")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id){
        boolean deleted = bookService.deleteBook(id);

        //404エラー発生
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        
        //204削除成功
        return ResponseEntity.noContent().build();
    }

    /**
     * POST/所蔵に該当書籍を１冊追加メソッド
     * @param id
     * @return 201 CopiesResponse
     */
    @PostMapping("/{id}/copies")
    @PreAuthorize("hasAnyRole('admin_employee')")
    public ResponseEntity<CopiesResponse> postCopies(@PathVariable Long id){

        BookCopy copy = bookService.createCopies(id);
        CopiesResponse response = new CopiesResponse(copy.getId(), copy.getStatus());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * DELETE/所蔵の1冊削除メソッド
     * @param id 書籍ID
     * @param copyId　所蔵ID
     * @return　204 
     */
    @DeleteMapping("/{id}/copies/{copyId}")
    @PreAuthorize("hasAnyRole('admin_employee')")
    public ResponseEntity<CopiesResponse> deleteCopies(@PathVariable Long id, @PathVariable Long copyId){

        bookService.deleteCopies(copyId);

        return ResponseEntity.noContent().build();
    }
}
