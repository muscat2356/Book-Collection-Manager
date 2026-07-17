package com.example.librashare.dto.response;

/**
 * LoansDetailsResponseにネストしてレスポンスするUserDTOクラス
 * @author furuyama
 * @since 2026-07-16
 * @see LoansDetailsResponse
 */
public class UserSummary {

    private Long id;
    private String displayName;

    public UserSummary() {
    }

    public UserSummary(Long id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public Long getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    
    
}
