package com.example.librashare.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.librashare.domain.Book;
import com.example.librashare.service.BookService;

/**
 * 書籍のCRUD機能を実装したRepository
 * JPAのため、interfaceで実装
 * @author furuyama
 * @since 2026-07-08
 * @see
 * BookService
 */
public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findAllByDeletedFalse();

    Optional<Book> findByIdAndDeletedFalse(Long id);
}
