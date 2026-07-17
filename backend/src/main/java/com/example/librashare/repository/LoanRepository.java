package com.example.librashare.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;

import com.example.librashare.domain.Loan;
import com.example.librashare.domain.LoanStatus;

import jakarta.persistence.LockModeType;

public interface LoanRepository extends JpaRepository<Loan, Long>{

    //一括取得/GET
    @EntityGraph(attributePaths = {"bookCopy", "bookCopy.book", "user"})
    List<Loan> findByStatus(LoanStatus status);

    //返却時の二重処理を防ぐための行ロックを取得
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Loan l JOIN FETCH l.bookCopy WHERE l.id = :id")
    Optional<Loan> findByIdForUpdate(@Param("id") Long id);
}
