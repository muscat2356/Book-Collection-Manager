package com.example.librashare.security;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import com.example.librashare.config.SecurityConfig;

/**
 * keycloakのアクセストークンを
 * SpringSecurityで活用できるように変換するクラス
 * @author furuyama
 * @since 2026-07-09
 * @see SecurityConfig
 */
@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {


    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
        new JwtGrantedAuthoritiesConverter();

    /**
     * keycloak発行のJwt を AbstractAuthenticationToken に変換するメソッド
     * Spring側がロールで活用できるために、GrantedAuthorityにロールを格納
     * 
     * @param　Jwt keycloak発行のJWT
     * @return JwtAuthenticationToken 標準実装クラスのJWT認証トークンにJwtとロールを付与
     */
    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt){
        
        //2つの場所から権限を集めて、1つにまとめる

        //jwtGrantedAuthoritiesConverter.convert(jwt)で`scope`クレームの権限取得
        //extractRealmsRole(jwt).stream()で、jwtのレルムロールの取得とロールを変換
       
        Collection<GrantedAuthority> authorities = Stream.concat(
            jwtGrantedAuthoritiesConverter.convert(jwt).stream(), 
            extractRealmsRole(jwt).stream()
            ).collect(Collectors.toSet());

        //authorities には「このユーザーが持つ全権限」が重複なく入った状態をSet

        return new JwtAuthenticationToken(jwt, authorities);
    }

    /**
     * Jwtのrealm_accessクレームからレルムロールのみを取得し、
     * GrantedAuthorityへ変換するメソッド
     * 
     * @param jwt keycloak発行のJWT
     * @return ROLE_プレフィックスを付与したレルムロールの集合。取得できない場合は空のSet
     */
    private Collection<GrantedAuthority> extractRealmsRole(Jwt jwt) {
        Map<String, Object> realmAccess;
        Collection<String> realmRoles;

        //getClaimの戻り値がObjectのため、Object型で取得
        Object rawRealmAccess = jwt.getClaim("realm_access");
        
        //ClassCastExceptionを避けるためMapチェック
        if (!(rawRealmAccess instanceof Map)) {
            return Set.of();
        }

        realmAccess = (Map<String, Object>)rawRealmAccess;

        //Mapからrolesのkeyを使って、roleを取得
        Object rawRealmRoles = realmAccess.get("roles");

        //ClassCastExceptionを避けるためCollectionチェック
        if (!(rawRealmRoles instanceof Collection)) {
            return Set.of();
        }

        realmRoles = (Collection<String>) rawRealmRoles;

        //Spring側がロールを確認する際に`"ROLE_"`が冒頭にないと認識されないため付与
        return realmRoles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_"+role))
                        .collect(Collectors.toSet());
    }
}
