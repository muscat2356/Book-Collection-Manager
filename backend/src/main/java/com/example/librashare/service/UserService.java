package com.example.librashare.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.librashare.domain.User;
import com.example.librashare.keycloak.KeycloakUserService;
import com.example.librashare.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class UserService {

    private final KeycloakUserService keycloakUserService;
    private final UserRepository userRepository;

    public UserService(KeycloakUserService keycloakUserService, UserRepository userRepository) {
        this.keycloakUserService = keycloakUserService;
        this.userRepository = userRepository;
    }

    //findAll
    public List<User> findAll(){
        return userRepository.findAll();
    }

    //findById
    public Optional<User> findById(Long id){
        return userRepository.findById(id);
    }

    //create
    @Transactional
    public User create(String email,String displayName){

        String sub = keycloakUserService.createUser(displayName, email);
        User user = new User(sub, displayName, email);

        return userRepository.save(user);

    }

    //update
    @Transactional
    public User update(Long id, String displayName, String email){

        Optional<User> optinalUser = userRepository.findById(id);

        if(optinalUser.isEmpty()){
            throw new EntityNotFoundException("User not found: id=" + id);
        }

        User user = optinalUser.get();

        user.setDisplayName(displayName);
        user.setEmail(email);
        
        return user;
    }

    //delete
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
