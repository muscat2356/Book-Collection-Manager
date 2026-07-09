package com.example.librashare.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.librashare.domain.Book;
import com.example.librashare.repository.BookRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> findAll() {
       return bookRepository.findAll();
    }

    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }


}
