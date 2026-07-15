package com.example.librashare.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.librashare.domain.User;
import com.example.librashare.dto.response.UserResponse;
import com.example.librashare.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

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

}
