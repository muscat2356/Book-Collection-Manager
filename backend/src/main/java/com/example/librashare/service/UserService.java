package com.example.librashare.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.librashare.domain.Loan;
import com.example.librashare.domain.LoanStatus;
import com.example.librashare.domain.User;
import com.example.librashare.exception.exception.BusinessException;
import com.example.librashare.keycloak.KeycloakUserService;
import com.example.librashare.repository.LoanRepository;
import com.example.librashare.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;


/**
 * ユーザーのCRUD機能を実装したService
 * @author furuyama
 * @since 2026-07-15
 * @see 
 * UserRepository
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final KeycloakUserService keycloakUserService;
    private final UserRepository userRepository;
    private final LoanRepository loanRepository;

    

    public UserService(KeycloakUserService keycloakUserService, UserRepository userRepository,
            LoanRepository loanRepository) {
        this.keycloakUserService = keycloakUserService;
        this.userRepository = userRepository;
        this.loanRepository = loanRepository;
    }

    /**
     * ユーザーの全件検索(activeユーザーのみ表示)
     * @return　DBからユーザーを全件リターン
     */
    public List<User> findAll(){
        return userRepository.findByIsActiveTrue();
    }

    /**
     * 該当ユーザーの検索
     * @param id
     * @return　DBから該当書籍IDをリターン（レスポンスで使用するため）
     */
    public Optional<User> findById(Long id){
        return userRepository.findByIdAndIsActiveTrue(id);
    }

    /**
     * 該当ユーザー登録Service
     * @param email,displayName
     * @return　登録ユーザーのリターン
     */
    @Transactional
    public User create(String email,String displayName){

        String sub = keycloakUserService.createUser(email);
        User user = new User(sub, displayName, email);

        return userRepository.save(user);

    }

    /**
     * 該当ユーザーの更新処理service側
     * @param id, displayName,　email
     * @return　ユーザー情報の送信
     */
    @Transactional
    public User update(Long id, String displayName, String email){

        Optional<User> optinalUser = userRepository.findByIdAndIsActiveTrue(id);

        //ユーザーが存在するのか確認 404　GlobalExceptionHandlerで捕捉
        if(optinalUser.isEmpty()){
            logger.error("ユーザーが存在しません id={}",id);
            throw new EntityNotFoundException("User not found: id=" + id);
        }
        
        User user = optinalUser.get();

        //メール重複確認 ->カスタム例外処理　409
        if (!user.getEmail().equals(email)&& userRepository.existsByEmail(email)){
            logger.error("メールアドレスが重複しています email={}",email);
            throw new BusinessException("EMAIL_ALREADY_EXISTS", "このメールアドレスは既に使用されています");
        }

        user.setDisplayName(displayName);
        user.setEmail(email);

        //DB更新処理開始
        userRepository.saveAndFlush(user);

        //keycloak更新処理開始
        keycloakUserService.updateEmail(user.getKeycloakSub(), email);

        logger.info("ユーザー更新完了 id={}", id);
        
        return user;
    }

    /**
     * 該当ユーザーの論理削除（EnableOFF設定）
     * @param id
     */
    @Transactional
    public void deleteUser(Long id){
        Optional<User> optinalUser = userRepository.findByIdAndIsActiveTrue(id);

        if(optinalUser.isEmpty()){
            logger.warn("ユーザーが存在しません id={}", id);
            throw new EntityNotFoundException("User not found: id=" + id);
        }

        User user = optinalUser.get();


        List<Loan> loanList = loanRepository.findByUserId(user.getId());

        boolean hasLoaded = loanList.stream()
                            .anyMatch(l -> l.getStatus() == LoanStatus.BORROWED);

        if(hasLoaded){
            logger.warn("書籍を貸出中のためユーザーを削除できません id={}", id);
            throw new BusinessException(
                    "USER_HAS_ACTIVE_LOANS", 
                    "書籍を貸出中のため、ユーザー削除ができません。");
        }

        user.setActive(false);

        userRepository.save(user);

        keycloakUserService.disableUser(user.getKeycloakSub());

    }
    
}
