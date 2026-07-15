package com.example.librashare.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserResponse {

    private Long id;
    private String keycloakSub;
    private String displayName;
    private String email;
    private boolean isActive;
   
    public UserResponse() {
    }

    public UserResponse(Long id, String keycloakSub, String displayName, String email, boolean isActive) {
        this.id = id;
        this.keycloakSub = keycloakSub;
        this.displayName = displayName;
        this.email = email;
        this.isActive = isActive;
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

    @JsonProperty("isActive")
    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

}
