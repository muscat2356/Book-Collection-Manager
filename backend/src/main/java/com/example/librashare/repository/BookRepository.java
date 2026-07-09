package com.example.librashare.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.librashare.domain.Book;

public interface BookRepository extends JpaRepository<Book, Long> {

}
