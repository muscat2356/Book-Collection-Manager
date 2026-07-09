package com.example.librashare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.librashare.domain.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

}
