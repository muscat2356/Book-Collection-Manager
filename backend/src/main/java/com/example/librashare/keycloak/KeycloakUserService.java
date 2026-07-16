package com.example.librashare.keycloak;

import java.util.List;

import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.librashare.exception.exception.KeycloakOperationException;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@Service
public class KeycloakUserService {

    //keycloak接続情報
    private final KeycloakProperties props;
    private final Logger logger = LoggerFactory.getLogger(KeycloakUserService.class);

    public KeycloakUserService(KeycloakProperties props) {
        this.props = props;
    }

    /**
     * keycloakのuser新規作成（一般ユーザーのみ）
     * @param username
     * @param email
     * @return　keycloakUserのID（DB登録で使用するため）
     */
    public String createUser(String email){

        //keycloak接続立ち上げ
        Keycloak keycloak = buildKeycloak();
        

        try{
            //ユーザー情報を表すオブジェクトの生成
            UserRepresentation user = new UserRepresentation();
            user.setUsername(email);
            user.setEmail(email);
            user.setEnabled(true);
            //ユーザーアクティブのON設定

            //keycloakへユーザー登録のAPIリクエスト
            Response response = keycloak.realm(props.getRealm()).users().create(user);

            //ReposenからLocationヘッダーを取得する -> keycloakID
            String sub = CreatedResponseUtil.getCreatedId(response);

            //ロール登録処理
            RoleRepresentation role = keycloak.realm(props.getRealm())
                    .roles()
                    .get("general_user")
                    .toRepresentation();

            //ロール付与の実施
            keycloak.realm(props.getRealm())
                    .users()
                    .get(sub)
                    .roles()
                    .realmLevel()
                    .add(List.of(role));
            
            //keycloakIDの付与
            return sub;

        }catch(WebApplicationException e){
            logger.error("Keycloakユーザー作成に失敗: email={}", email, e);
            throw new KeycloakOperationException(
                    "KEYCLOAK_USER_CREATED_FAILED", 
                    "keycloakユーザー作成またはロール付与処理に失敗しました。");
        }finally{
            keycloak.close();
        }
    }


    /**
     * keycloakメールアドレス更新処理メソッド
     * @param keycloakSub
     * @param email
     * @throws KeycloakOperationException →500エラー
     */
    public void updateEmail(String keycloakSub, String email) {
        Keycloak keycloak = buildKeycloak();

        try{
            //ユーザー情報の取得
            UserRepresentation user = keycloak.realm(props.getRealm())
                                    .users()
                                    .get(keycloakSub)
                                    .toRepresentation();
            
            //変更箇所の情報をセット
            user.setEmail(email);
            user.setUsername(email);

            //ユーザー更新処理の実行
            keycloak.realm(props.getRealm()).users().get(keycloakSub).update(user);

        }catch(WebApplicationException e){
            logger.error("keycloakのユーザー更新処理に失敗しました。 keycloakID={}", keycloakSub, e);
           
            throw new KeycloakOperationException(
                "KEYCLOAK_USER_UPDATED_FAILED",
                "Keycloakのメール更新処理が失敗しました。");

        }finally{
            keycloak.close();
        }
        
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
