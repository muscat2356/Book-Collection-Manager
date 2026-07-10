package com.example.librashare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.librashare.domain.Book;

/**
 * 書籍のCRUD機能を実装したRepository
 * JPAのため、interfaceで実装
 * @author furuyama
 * @since 2026-07-08
 * @see
 * BookService
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

}
