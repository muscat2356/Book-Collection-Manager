package com.example.librashare.service;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.librashare.controller.LoansController;
import com.example.librashare.domain.BookCopy;
import com.example.librashare.domain.Loan;
import com.example.librashare.domain.LoanStatus;
import com.example.librashare.domain.User;
import com.example.librashare.exception.exception.BusinessException;
import com.example.librashare.exception.exception.CopyNotAvailableException;
import com.example.librashare.repository.BookCopyRepository;
import com.example.librashare.repository.LoanRepository;
import com.example.librashare.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;


/**
 * 貸出処理のserviceクラス
 * @author furuyama
 * @since 2026-07-16
 * @see LoansController
 * @see Loansrepository
 */
@Service
public class LoansService {

    private static final Logger logger = LoggerFactory.getLogger(LoansService.class);
    
    private final LoanRepository loanRepository;
    private final BookCopyRepository bookCopyRepository;
    private final UserRepository userRepository;

    

    public LoansService(LoanRepository loanRepository, BookCopyRepository bookCopyRepository,
            UserRepository userRepository) {
        this.loanRepository = loanRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.userRepository = userRepository;
    }

    /**
     * 貸出中書籍の全権検索
     * @return
     */
    @Transactional(readOnly = true)
    public List<Loan> findActiveLoan(){
        return loanRepository.findByStatus(LoanStatus.BORROWED);
    }


    /**
     * 貸出処理の一括実装
     * @param userId
     * @param bookCopyIds
     * @return
     * @throws BusinessException
     */
    @Transactional
    public List<Loan> createLoans(Long userId, List<Long> bookCopyIds){

        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty()) {
            throw new BusinessException("USER_NOT_FOUND", "該当ユーザーが見つかりません");
        }

        User user = optionalUser.get();

        if (!user.isActive()) {
            throw new BusinessException("USER_NOT_ACTIVE", "該当ユーザーは貸し出しできません");
        }

        List<BookCopy> bookCopies = bookCopyRepository.findByIdsForUpdate(bookCopyIds);

        if (bookCopies.size() != bookCopyIds.size()) {
            throw new EntityNotFoundException("BookCopy not found. requested=" + bookCopyIds);
        }

        Set<Long> requestBookIds = new HashSet<>();
        Set<Long> borrowedBookIds = loanRepository.findBorrowedBookIdsByUserId(userId, LoanStatus.BORROWED);

        boolean loanBook = bookCopies.stream()
                                    .anyMatch(b -> !requestBookIds.add(b.getBook().getId())
                                                || borrowedBookIds.contains(b.getBook().getId()));

        if (loanBook) {
            throw new BusinessException("BOOK_ALREADY_LOANED_BY_USER", "同じ書誌は一人一冊までです");
        }
        
        List<Long> failedId = bookCopies.stream()
                                .filter(copy -> !copy.isAvailable())
                                .map(BookCopy::getId)
                                .toList();
        
        if (!failedId.isEmpty()) {
            logger.warn("貸出不可の所蔵が含まれています。failedIds={}", failedId);
            throw new CopyNotAvailableException(failedId);
        }
        
        OffsetDateTime now = OffsetDateTime.now();

        List<Loan> loans = bookCopies.stream()
                .map(copy -> Loan.borrow(copy, user, now))
                .toList();

        List<Loan> saved = loanRepository.saveAll(loans);


        return saved;
    }

    /**
     * 貸出処理の返却処理メソッド
     * @param id
     * @return
     * @throws EntityNotFoundException
     * @throws BusinesException
     */
    @Transactional
    public Loan deleteLoan(Long id){

        Optional<Loan> optionalLoan = loanRepository.findByIdForUpdate(id);

        if (optionalLoan.isEmpty()) {
            throw new EntityNotFoundException();
        }

        Loan loan = optionalLoan.get();

        if (loan.isReturned()) {
            throw new BusinessException("LOAN_ALREADY_RETURNED", "既に返却済みです。");
        }
        
        loan.returnBook(OffsetDateTime.now());

        return loan;

    }

}
