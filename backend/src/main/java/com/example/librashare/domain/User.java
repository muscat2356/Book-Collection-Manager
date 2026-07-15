package com.example.librashare.domain;

import java.time.Instant;

import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Userのdomainクラス　Usersテーブルとの連携を兼ねてアノテーション付与
 * @author furuyama
 * @since 2026-07-15
 * @see UserController
 */
@Entity
@Table(name="users", uniqueConstraints = {
    @UniqueConstraint(name = "uq_users_email", columnNames = "email"),
    @UniqueConstraint(name = "uq_users_keycloak_sub", columnNames = "keycloak_sub"),
})
//このエンティティに対して INSERT / UPDATE などが起きたとき、指定したクラスに処理を割り込ませる
//Spring Data JPA が用意しているリスナー　-> AuditingEntityListener
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //keycloakのID
    @Column(name = "keycloak_sub", nullable =  false, length = 36)
    private String keycloakSub;

    @Column(name = "display_name",nullable = false, length = 100)
    private String displayName;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "is_active",nullable = false)
    private boolean isActive = true;

    //更新日時処理の自動化
    @LastModifiedDate
    @Column(name = "updated_at" ,nullable = false)
    private Instant updatedAt;

    public User() {
    }

    public User(String keycloakSub, String displayName, String email) {
        this.keycloakSub = keycloakSub;
        this.displayName = displayName;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getKeycloakSub() {
        return keycloakSub;
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

    
}
