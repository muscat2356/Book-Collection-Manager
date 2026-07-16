package com.example.librashare.keycloak;

import java.security.SecureRandom;
import java.util.List;

import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.CredentialRepresentation;
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
    
    //一時的なパスワード作成に使用
    private static final String WORD = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz23456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ROLE = "general_user";

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
                String sub = registerUser(keycloak, email);
                try{
                    assignRole(keycloak, sub);
                }catch(RuntimeException e){
                    deleteUser(keycloak, sub);
                    throw e;
                }
                return sub;
            }finally{
                keycloak.close();
            }
    }

    /**
     * keycloakユーザー作成用のユーザー組み立てメソッド
     * @param email
     * @return
     */
    private UserRepresentation buildUserRepresentation(String email){

        //ユーザーをビルド
        UserRepresentation user = new UserRepresentation();

            user.setUsername(email);
            user.setEmail(email);
            user.setEnabled(true);
            user.setCredentials(List.of(temporaryPasswordCreate()));
            //一時的なパスワード取得

        return user;
    }

    /**
     * keycloakユーザー作成時の一時的なパスワード生成メソッド
     * @return
     */
    private CredentialRepresentation temporaryPasswordCreate() {

       CredentialRepresentation credential = new CredentialRepresentation();
        //パスワード設定
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setTemporary(true);
        credential.setValue(generateTemporaryPassword(16));

        return credential;
    }

    /**
     * keycloakユーザー作成のユーザー登録メソッド
     * @param keycloak
     * @param email
     * @return
     */
    private String registerUser(Keycloak keycloak, String email){

        Response response;

        try{
            //ユーザー作成のPOSTメソッドを実行
            response = keycloak.realm(props.getRealm())
                                    .users()
                                    .create(buildUserRepresentation(email));
        }catch(WebApplicationException e){

            logger.error("Keycloakユーザー作成に失敗: email={}", email, e);

            throw new KeycloakOperationException(
                    "KEYCLOAK_USER_CREATED_FAILED", 
                    "keycloakユーザー作成に失敗しました。");
        }

        try{
                //メール重複確認
                if(response.getStatus() == 409 ){
                    logger.error("既に登録されているメールアドレスを使用しています。");
                    throw new KeycloakOperationException(
                                "USER_ALREADY_EXISTS",
                                "既に登録されているメールアドレスです");
                }

                //その他例外の確認
                if(response.getStatus() != 201){
                    logger.error("keycloakのユーザー登録に失敗しました。");
                    throw new KeycloakOperationException(
                            "USER_CREATED_FAILED", 
                            "keycloakのユーザー登録に失敗しました。");
                }
            return CreatedResponseUtil.getCreatedId(response);
        }finally{
            response.close();
        }
    }

    /**
     * keycloakユーザー作成のロール付与実行メソッド
     * @param keycloak
     * @param sub
     */
    private void assignRole(Keycloak keycloak, String sub){
        try{

            //ロールの設定
            RoleRepresentation role = keycloak.realm(props.getRealm())
                .roles()
                .get(ROLE)
                .toRepresentation();

            //ロール付与の実施
            keycloak.realm(props.getRealm())
                    .users()
                    .get(sub)
                    .roles()
                    .realmLevel()
                    .add(List.of(role));

        }catch(WebApplicationException e){

            logger.error("Keycloakユーザーロール付与に失敗 sub={}", sub, e);
            throw new KeycloakOperationException(
                    "KEYCLOAK_USER_ASSIGNROLE_FAILED", 
                    "keycloakロール付与処理に失敗しました。");
        }
    }

    /**
     * keycloakユーザー作成時にロール付与失敗ユーザーの削除補助メソッド
     * 作成失敗の場合に削除を実行し、再度登録を促す設定
     */
    private void deleteUser(Keycloak keycloak, String sub){

        try{
            
            //keycloakへDELETEメソッドの実行依頼
            keycloak.realm(props.getRealm()).users().get(sub).remove();
            logger.info("ロール付与失敗のためユーザーを削除しました sub={}", sub);

        }catch(RuntimeException e){
            logger.error("ロール付与失敗のユーザー削除が実行できませんでした（手動で削除対応をお願いします） sub={}", sub, e);
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


    private String generateTemporaryPassword(int length){

        StringBuilder sb = new StringBuilder(length);
        
        for(int i = 0; i< length ; i++){
            sb.append(WORD.charAt(RANDOM.nextInt(WORD.length())));
        }
        
        return sb.toString();
    }

}
