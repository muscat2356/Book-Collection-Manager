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

/**
 * JwtAuthConverter
 */
@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
        new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt){
        Collection<GrantedAuthority> authorities = Stream.concat(
            jwtGrantedAuthoritiesConverter.convert(jwt).stream(), 
            extractRealmsRole(jwt).stream()
            ).collect(Collectors.toSet());

        return new JwtAuthenticationToken(jwt, authorities);
    }

    private Collection<GrantedAuthority> extractRealmsRole(Jwt jwt) {
        Map<String, Object> realmAccess;
        Collection<String> realmRoles;

        Object rawRealmAccess = jwt.getClaim("realm_access");
        if (!(rawRealmAccess instanceof Map)) {
            return Set.of();
        }

        realmAccess = (Map<String, Object>)rawRealmAccess;

        Object rawRealmRoles = realmAccess.get("roles");
        if (!(rawRealmRoles instanceof Collection)) {
            return Set.of();
        }

        realmRoles = (Collection<String>) rawRealmRoles;

        return realmRoles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_"+role))
                        .collect(Collectors.toSet());
    }
}
