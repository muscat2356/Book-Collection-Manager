package com.example.librashare.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.librashare.domain.User;
import com.example.librashare.exception.exception.BusinessException;
import com.example.librashare.keycloak.KeycloakUserService;
import com.example.librashare.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

/**
 * ユーザーのCRUD機能を実装したService
 * @author furuyama
 * @since 2026-07-15
 * @see 
 * UserRepository
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakUserService.class);

    private final KeycloakUserService keycloakUserService;
    private final UserRepository userRepository;

    public UserService(KeycloakUserService keycloakUserService, UserRepository userRepository) {
        this.keycloakUserService = keycloakUserService;
        this.userRepository = userRepository;
    }

    /**
     * ユーザーの全件検索
     * @return　DBからユーザーを全件リターン
     */
    public List<User> findAll(){
        return userRepository.findAll();
    }

    /**
     * 該当ユーザーの検索
     * @param id
     * @return　DBから該当書籍IDをリターン（レスポンスで使用するため）
     */
    public Optional<User> findById(Long id){
        return userRepository.findById(id);
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

        Optional<User> optinalUser = userRepository.findById(id);

        if(optinalUser.isEmpty()){
            logger.error("ユーザーが存在しません id:"+id);
            throw new EntityNotFoundException("User not found: id=" + id);
        }

        User user = optinalUser.get();

        if (!user.getEmail().equals(email)&& userRepository.existsByEmail(email)){
            logger.error("メールアドレスが重複しています email={}",email);
            throw new BusinessException("EMAIL_ALREADY_EXISTS", "このメールアドレスは既に使用されています");
        }


        user.setDisplayName(displayName);
        user.setEmail(email);

        userRepository.saveAndFlush(user);

        keycloakUserService.updateEmail(user.getKeycloakSub(), email);
        
        return user;
    }

    /**
     * 該当ユーザーの論理削除（EnableOFF設定）
     * @param id
     */
    @Transactional
    public void deleteUser(Long id){
        Optional<User> optinalUser = userRepository.findById(id);

        if(optinalUser.isEmpty()){
            throw new EntityNotFoundException("User not found: id=" + id);
        }

        User user = optinalUser.get();

        user.setActive(false);

        userRepository.save(user);

        keycloakUserService.disableUser(user.getKeycloakSub());

    }
    
}
