package com.example.librashare.controller;

import com.example.librashare.service.UserService;

import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.librashare.domain.Book;
import com.example.librashare.domain.BookCopy;
import com.example.librashare.domain.Loan;
import com.example.librashare.domain.User;
import com.example.librashare.dto.request.LoansRequest;
import com.example.librashare.dto.response.BookSummary;
import com.example.librashare.dto.response.LoansDetailsResponse;
import com.example.librashare.dto.response.LoansPostListResponse;
import com.example.librashare.dto.response.LoansResponse;
import com.example.librashare.dto.response.UserSummary;
import com.example.librashare.service.LoansService;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/loans")
public class LoansController {

    private final UserService userService;
    private LoansService loansService;

    public LoansController(LoansService loansService, UserService userService) {
        this.loansService = loansService;
        this.userService = userService;
    }

    /**
     * 貸出中書籍の全件検索メソッド
     * @return
     */
    @GetMapping("/active")
    public ResponseEntity<List<LoansDetailsResponse>> findAllActive(){

        List<Loan> loans = loansService.findActiveLoan();

        List<LoansDetailsResponse> responses = new ArrayList<>();

        for (Loan loan : loans) {
            responses.add(toLoansDetailsResponse(loan));
        }

        return ResponseEntity.ok(responses);
    }

    /**
     * 書籍の貸出処理メソッド
     * @param loansRequest
     * @return
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('general_employee','admin_employee')")
    public ResponseEntity<LoansPostListResponse> loansInsert(@Valid @RequestBody LoansRequest loansRequest){
        
       List<Loan> loans = loansService.createLoans(loansRequest.getUserId(), loansRequest.getBookCopyIds());

       List<LoansResponse> responses = new ArrayList<>();

       for (Loan loan : loans) {
            responses.add(toLoansResponse(loan));
       }

       LoansPostListResponse body = new LoansPostListResponse(responses);

        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    
    /**
     * 書籍返却処理メソッド
     * @param id
     * @return
     */
    @PutMapping("/{id}/return")
    public ResponseEntity<LoansResponse> returnBook(@PathVariable Long id){

        Loan loan = loansService.deleteLoan(id);

        return ResponseEntity.ok(toLoansResponse(loan));
    } 

    /**
     * Entityからレスポンスへの変換処理メソッド（LoansResponse）
     * @param loan
     * @return
     */
    private LoansResponse toLoansResponse(Loan loan) {
        BookCopy copy = loan.getBookCopy();
        Book book = copy.getBook();

        return new LoansResponse(
                loan.getId(),
                copy.getId(),
                book.getId(),
                book.getTitle(),
                loan.getUser().getId(),
                loan.getBorrowedAt(),
                loan.getReturnedAt(),
                loan.getStatus());
        
    }

    /**
     * Entityからレスポンスへの変換処理メソッド（LoansDetailsResponse）
     * @param loan
     * @return
     */
    private LoansDetailsResponse toLoansDetailsResponse(Loan loan){
        BookCopy copy = loan.getBookCopy();
        Book book = copy.getBook();
        User user = loan.getUser();

        BookSummary bookSummary = new BookSummary(
                book.getId(),
                book.getTitle(),
                book.getAuthor());

        UserSummary userSummary = new UserSummary(
                user.getId(),
                user.getDisplayName());

        return new LoansDetailsResponse(
                loan.getId(),
                copy.getId(),
                bookSummary,
                userSummary,
                loan.getBorrowedAt(),
                loan.getReturnedAt(),
                loan.getStatus());
    }

}
