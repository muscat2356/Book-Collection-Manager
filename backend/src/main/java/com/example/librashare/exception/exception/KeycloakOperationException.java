package com.example.librashare.exception.exception;

/**
 *keycloak内の例外発生時のカスタム例外クラス
 @author furuyama
 @since 2026-07-16
 @see KeycloakUserService
 */
public class KeycloakOperationException extends RuntimeException {

    private final String error;

    public KeycloakOperationException(String error, String message) {
        super(message);
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
