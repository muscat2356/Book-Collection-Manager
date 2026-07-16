package com.example.librashare.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.librashare.domain.User;
import com.example.librashare.dto.request.UserRequest;
import com.example.librashare.dto.response.UserResponse;
import com.example.librashare.service.UserService;

import jakarta.validation.Valid;

/**
 * UserAPIの処理
 * @author furuyama
 * @since 2026-07-14
 * @see UserService
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * ユーザー全件取得API（activeユーザーのみ取得）
     * @return　ユーザー全件のjsonデータをレスポンス
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('general_employee','admin_employee')")
    public ResponseEntity<List<UserResponse>> findAll(){
        List<User> users = userService.findAll();

        List<UserResponse> responses = users.stream()
                            .map(user -> {
                                UserResponse response = new UserResponse();
                                response.setId(user.getId());
                                response.setKeycloakSub(user.getKeycloakSub());
                                response.setDisplayName(user.getDisplayName());
                                response.setEmail(user.getEmail());
                                response.setActive(user.isActive());
                                return response;
                            })
                            .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * 該当ユーザー取得API
     * @param id
     * @return　該当ユーザーのjsonデータ/存在しない場合に404/notfoundをレスポンス
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('general_employee','admin_employee')")
    public ResponseEntity<UserResponse> findById(@PathVariable Long id){

        Optional<User> find = userService.findById(id);

        if(find.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        User user = new User();

        user = find.get();

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setKeycloakSub(user.getKeycloakSub());
        response.setDisplayName(user.getDisplayName());
        response.setEmail(user.getEmail());
        response.setActive(user.isActive());

        return ResponseEntity.ok(response);

    }

    /**
     * 該当ユーザー登録API
     * @param UserRequest userRequest
     * @return　Response 201　作成したユーザーの情報を送信
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('general_employee','admin_employee')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest userRequest){
        
        User user = userService.create(userRequest.getEmail(), userRequest.getDisplayName());
        
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setKeycloakSub(user.getKeycloakSub());
        response.setDisplayName(user.getDisplayName());
        response.setEmail(user.getEmail());
        response.setActive(user.isActive());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }
    
    /**
     * 該当ユーザーの全更新処理API
     * @param id
     * @param userRequest
     * @return　成功時：200 存在しない場合：404
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('general_employee','admin_employee')")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserRequest userRequest){

        User user = userService.update(id, userRequest.getDisplayName(), userRequest.getEmail());

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setKeycloakSub(user.getKeycloakSub());
        response.setDisplayName(user.getDisplayName());
        response.setEmail(user.getEmail());
        response.setActive(user.isActive());

        return ResponseEntity.ok(response);
    }

    /**
     * 該当ユーザーの削除処理API
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('general_employee','admin_employee')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id){

        userService.deleteUser(id);
        
        return ResponseEntity.noContent().build();
    }
}