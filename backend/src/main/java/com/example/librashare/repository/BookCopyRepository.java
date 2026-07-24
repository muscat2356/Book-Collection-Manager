package com.example.librashare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.librashare.domain.BookCopy;
import com.example.librashare.service.BookService;

import jakarta.persistence.LockModeType;

import java.util.List;


/**
 * 書籍のCRUD機能を実装したRepository
 * JPAのため、interfaceで実装
 * @author furuyama
 * @since 2026-07-08
 * @see
 * BookService
 */
public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {
    
    // デッドロック回避のためID昇順でロックを取得
    //両方のリクエストを待たないように設定
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM BookCopy c JOIN FETCH c.book WHERE c.id IN :ids AND c.deleted = false ORDER BY c.id")
    List<BookCopy> findByIdsForUpdate(@Param("ids") List<Long> ids);

    List<BookCopy> findByBookIdAndDeletedFalse(Long id);
    }
