package com.example.librashare.keycloak;

import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import jakarta.ws.rs.core.Response;

@Service
public class KeycloakUserService {

    //keycloak接続情報
    private final KeycloakProperties props;

    public KeycloakUserService(KeycloakProperties props) {
        this.props = props;
    }

    /**
     * keycloakのuser新規作成（一般ユーザーのみ）
     * @param username
     * @param email
     * @return　keycloakUserのID（DB登録で使用するため）
     */
    public String createUser(String username, String email){

        //keycloak接続立ち上げ
        Keycloak keycloak = buildKeycloak();
        
        //ユーザー情報を表すオブジェクトの生成
        UserRepresentation user = new UserRepresentation();
        user.setEmail(email);
        user.setEnabled(true);
        //ユーザーアクティブのON設定

        //keycloakへユーザー登録のAPIリクエスト
        Response response = keycloak.realm(props.getRealm()).users().create(user);

        //ReposenからLocationヘッダーを取得する -> keycloakID
        String sub = CreatedResponseUtil.getCreatedId(response);

        keycloak.close();

        return sub;
    }

    /**
     * keycloakのuser削除(論理削除)メソッド
     * Enableをoffに更新
     * @param sub　該当userのkeycloakID
     */
    public void disableUser(String sub){

        Keycloak keycloak = buildKeycloak();

        try{
            //keycloak上に存在するuser情報をオブジェクトとして取得
            UserRepresentation user = keycloak.realm(props.getRealm())
                                            .users().get(sub).toRepresentation();
            user.setEnabled(false);

            //keycloakへuser更新処理の実行
            keycloak.realm(props.getRealm()).users().get(sub).update(user);
        }finally{
            keycloak.close();
        }

    }

    /**
     * keycloak接続処理メソッド
     * @return　keycloak接続をリターン
     */
    public Keycloak buildKeycloak(){
        return KeycloakBuilder.builder()
                .serverUrl(props.getServerUrl())
                .realm(props.getRealm())
                .clientId(props.getClientId())
                .clientSecret(props.getClientSecret())
                .grantType("client_credentials")
                .build();

        //ymlに登録されている情報をもとに、接続処理を実行
        //clientIdとclientSecretの双方を活用したclient_credentialsでの接続処理を採用
    }


}
