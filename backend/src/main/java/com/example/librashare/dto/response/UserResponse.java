package com.example.librashare.dto.response;

import java.time.Instant;

public class UserResponse {

    private Long id;
    private String keycloakSub;
    private String displayName;
    private String email;
    private boolean isActive;
    private Instant updatedAt;
    
    public UserResponse() {
    }

    public UserResponse(Long id, String keycloakSub, String displayName, String email, boolean isActive,
            Instant updatedAt) {
        this.id = id;
        this.keycloakSub = keycloakSub;
        this.displayName = displayName;
        this.email = email;
        this.isActive = isActive;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKeycloakSub() {
        return keycloakSub;
    }

    public void setKeycloakSub(String keycloakSub) {
        this.keycloakSub = keycloakSub;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    

}
