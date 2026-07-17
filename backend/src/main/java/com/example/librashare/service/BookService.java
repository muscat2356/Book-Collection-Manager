package com.example.librashare.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.librashare.domain.Book;
import com.example.librashare.domain.BookCopy;
import com.example.librashare.domain.CopyStatus;
import com.example.librashare.dto.request.BookRequest;
import com.example.librashare.dto.response.BookResponse;
import com.example.librashare.dto.response.CopiesResponse;
import com.example.librashare.dto.response.HoldingResponse;
import com.example.librashare.exception.exception.BusinessException;
import com.example.librashare.repository.BookCopyRepository;
import com.example.librashare.repository.BookRepository;

import jakarta.persistence.EntityNotFoundException;
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
    private final BookCopyRepository copyRepository;

    public BookService(BookRepository bookRepository, BookCopyRepository copyRepository) {
        this.bookRepository = bookRepository;
        this.copyRepository = copyRepository;
    }

    /**
     * 書籍の全件検索
     * @return　DBから書籍を全件リターン
     */
    public List<BookResponse> findAll() {
       return bookRepository.findAll().stream()
       .filter(book -> !book.isDeleted())
       .map(book -> toResponse(book, false))
       .toList();
    }

    /**
     * 該当書籍の検索
     * @param id
     * @return　DBから該当書籍IDをリターン（レスポンスで使用するため）
     */
    public Optional<BookResponse> findById(Long id) {
        return bookRepository.findById(id)
        .filter(b -> !b.isDeleted())
        .map(b -> toResponse(b, true));

    }

    /**
     * 該当書籍登録Service
     * @param BookRequest bookRequest　該当書籍の情報
     * @return　Response 201　作成した書籍の情報を送信
     */
    public BookResponse createBook(BookRequest request) {
        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        Book saved = bookRepository.save(book);

        // Optionlで包んで値を確認、nullの場合は0をセット
        int copies = Optional.ofNullable(request.getInitialCopyCount()).orElse(0);

        List<BookCopy> newCopies = IntStream.range(0, copies)
        .mapToObj(i -> new BookCopy(null, saved.getId(), CopyStatus.AVAILABLE))
        .toList();

        copyRepository.saveAll(newCopies);

        return toResponse(saved, false);


    }

    /**
     * 該当書籍の更新処理service側
     * @param id
     * @param request
     * @return　Optionalの中にresponseを入れてリターン
     */
    public Optional<BookResponse> updateBook(Long id, BookRequest request) {
        return bookRepository.findById(id)
            .filter(b -> !b.isDeleted())
            .map(book -> {
                book.setTitle(request.getTitle());
                book.setAuthor(request.getAuthor());
                book.setIsbn(request.getIsbn());
                return toResponse(book, false);
            });
    }

    /**
     * 書籍domainをresponseへ変換メソッド
     * @param book
     * @return　BookResponseを返還
     */
    private BookResponse toResponse(Book book, boolean incluedeHoldings) {
        List<BookCopy> copies = copyRepository.findByBookId(book.getId());

        // 確認しないとここで例外？
        int total = copies.size();

        int availableCount = (int) copies.stream()
            .filter(c -> c.getStatus() == CopyStatus.AVAILABLE)
            .count();

            List<HoldingResponse> holdings = new ArrayList<>();
            if (incluedeHoldings) {
                holdings = copies.stream()
                    .map(c -> new HoldingResponse(c.getId(), c.getStatus()))
                    .toList();
            }

            return new BookResponse(
                book.getId(), 
                book.getTitle(), 
                book.getAuthor(),
                book.getIsbn(),
                total,
                availableCount,
                holdings);
    }

    /**
     * 該当書籍の削除処理
     * @param id
     * @return 
     * @throws BusinessException
     */
    public boolean deleteBook(Long id) {

        //該当書籍が存在するのか確認
        Optional<Book> find = bookRepository.findById(id);
        if (find.isEmpty()) {
            //存在しない場合にfalse
            return false;
        }
        Book book = find.get();
        //bookの論理削除フラグを更新

        //貸出をされている場合に削除できないように例外処理
        //bussinessExceptionの例外を発生させる

        book.setDeleted(true);

        bookRepository.save(book);

        return true;
    }

    /**
     * 所蔵の追加　PUT
     * @param id
     * @return
     */
    public BookCopy createCopies(Long id) {

        Optional<Book> book = bookRepository.findById(id);

        //空チェック 404
        if(book.isEmpty()){
            throw new EntityNotFoundException();
        }

        BookCopy copy = new BookCopy();

        copy.setBookId(id);
        copy.setStatus(CopyStatus.AVAILABLE);

        return copyRepository.save(copy);
    }

    /**
     * 所蔵の1冊を削除する処理 (物理削除)
     * @param id 該当所蔵書籍
     */
    public void deleteCopies(Long id){

        Optional<BookCopy> copy = copyRepository.findById(id);

        //空チェック 404
        if(copy.isEmpty()){
            throw new EntityNotFoundException();
        }

        BookCopy book = copy.get();
        book.setStatus(CopyStatus.LOANED);

        if(book.getStatus() == CopyStatus.LOANED){
            throw new BusinessException(
                    "COPY_NOT_DELETABLE", 
                    "貸出中、または貸出履歴がある所蔵のため削除できません");
        }

        copyRepository.delete(book);

    }
    
    


}
