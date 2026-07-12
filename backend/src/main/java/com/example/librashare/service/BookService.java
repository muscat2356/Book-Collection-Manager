package com.example.librashare.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.librashare.domain.Book;
import com.example.librashare.dto.request.BookRequest;
import com.example.librashare.dto.response.BookResponse;
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
        Book book = new Book();
        
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setStockCount(request.getStockCount());

        Book saved = bookRepository.save(book);
        return saved.getId();
    }

    /**
     * 該当書籍の更新処理service側
     * @param id
     * @param request
     * @return　Optionalの中にresponseを入れてリターン
     */
    public Optional<BookResponse> updateBook(Long id, BookRequest request) {
        
        //該当書籍が存在するのか確認
        //null対策
        Optional<Book> find = bookRepository.findById(id);
        if (find.isEmpty()) {
            //存在しない場合に空を返す
            return Optional.empty();
        }

        Book book = find.get();

        //更新内容へ入れ替え
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setStockCount(request.getStockCount());

       Book saved = bookRepository.save(book);
       
       BookResponse response = toResponse(saved);

       //Optionalで包んでリターン
       return Optional.of(response);
    }

    /**
     * 書籍domainをresponseへ変換メソッド
     * @param book
     * @return　BookResponseを返還
     */
    private BookResponse toResponse(Book book) {
    return new BookResponse(book.getId(), book.getTitle(), book.getAuthor(),
            book.getIsbn(), book.getStockCount());
    }

    public boolean deleteBook(Long id) {

        Optional<Book> find = bookRepository.findById(id);
        if (find.isEmpty()) {
            //存在しない場合に空を返す
            return false;
        }
        Book book = find.get();
        //bookの論理削除フラグを更新

        Book saved = bookRepository.save(book);

        return true;
    }


}
