package com.example.librashare.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.librashare.domain.Book;
import com.example.librashare.service.BookService;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }


    @GetMapping("")
    public ResponseEntity<List<Book>> findAll(){
        List<Book> books = bookService.findAll();

        return ResponseEntity.ok(books);
    }

}
