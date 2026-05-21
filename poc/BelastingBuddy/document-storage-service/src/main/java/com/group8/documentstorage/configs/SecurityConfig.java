package com.group8.documentstorage.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    /**
     * JWKS endpoints for Keycloak user realm
     */
    @Value("${app.keycloak.users.jwks:default-from-.env}")
    private String usersJwks;
    /**
     * JWKS endpoints for Keycloak service realm
     */
    @Value("${app.keycloak.services.jwks:default-from-.env}")
    private String servicesJwks;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/internal/**").hasAuthority("CALLER_SERVICE")
                        .anyRequest().authenticated()
                )
            .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder())
                                .jwtAuthenticationConverter(this::toAuthorities))
            );
        return http.build();
    }


    @Bean
    JwtDecoder jwtDecoder() {
        JwtDecoder users = NimbusJwtDecoder.withJwkSetUri(usersJwks).build();
        JwtDecoder svcs  = NimbusJwtDecoder.withJwkSetUri(servicesJwks).build();

        return token -> {
            try {
                return users.decode(token);
            } catch (Exception ignore) {}
            return svcs.decode(token);
        };
    }

    private AbstractAuthenticationToken toAuthorities(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        String iss = jwt.getClaimAsString("iss"); // safe, always String

        if (iss != null && iss.contains("/realms/services")) {
            String clientId = jwt.getClaimAsString("azp");
            authorities.add(new SimpleGrantedAuthority("SVC_" + clientId));
            authorities.add(new SimpleGrantedAuthority("CALLER_SERVICE"));
        } else if (iss != null && iss.contains("/realms/users")) {
            authorities.add(new SimpleGrantedAuthority("USER_AUTHENTICATED"));
        }

        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }

}
