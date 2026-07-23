package com.example.librashare.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.librashare.domain.User;
import com.example.librashare.service.UserService;

/**
 * UserDB処理の実行インターフェース
 * @author furuyama
 * @since 2026-07-15
 * @see UserService
 */
public interface UserRepository extends JpaRepository<User, Long> {
    
    boolean existsByEmail(String email);

    List<User> findByIsActiveTrue();

    Optional<User> findByIdAndIsActiveTrue(Long id);
}
