package com.vn.document.config;

import com.vn.document.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {
    @Value("${jwt.base64-secret}")
    private String jwtKey;
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            CustomAuthenticationEntryPoint customAuthenticationEntryPoint
    ) throws Exception {
        http
                .cors(Customizer.withDefaults()) // config cors
                .csrf(c -> c.disable())
                .authorizeHttpRequests(
                        authz -> authz
//                                .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                                //.requestMatchers( "/api/topics").permitAll()
//                                .requestMatchers("/", "/api/auth/login", "/api/auth/refresh").permitAll()
//                                .requestMatchers(HttpMethod.GET, "/api/users").hasAuthority("ROLE_ADMIN")
//                                 .anyRequest().authenticated())
                                .anyRequest().permitAll())
//                .oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults())
//                        .authenticationEntryPoint(customAuthenticationEntryPoint))
//                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults())
//                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                .exceptionHandling(
                        exceptions -> exceptions
                                .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint()) //401
                                .accessDeniedHandler(new BearerTokenAccessDeniedHandler())) //403
                .formLogin(f -> f.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

//        return http.build();
        return http.build();
    }
//    @Bean
//    public JwtAuthenticationConverter jwtAuthenticationConverter() {
//        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new
//                JwtGrantedAuthoritiesConverter();
//        // Đổi authorities từ "user.role" thay vì toàn bộ object "user"
//        grantedAuthoritiesConverter.setAuthoritiesClaimName("user.role");
//
//        // Thêm tiền tố ROLE_ để Spring Security nhận diện đúng quyền
//        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
//        JwtAuthenticationConverter jwtAuthenticationConverter = new
//                JwtAuthenticationConverter();
//        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
//        return jwtAuthenticationConverter;
//    }
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
            grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

            // Lấy danh sách quyền từ "roles"
            Collection<String> roles = jwt.getClaimAsStringList("roles");
            if (roles == null) {
                roles = Collections.emptyList();
            }

            // ✅ Chuyển đổi thành danh sách GrantedAuthority
            List<GrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role)) // Thêm tiền tố ROLE_
                    .collect(Collectors.toList());

            return authorities;
        });

        return jwtAuthenticationConverter;
    }


    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(
                getSecretKey()).macAlgorithm(SecurityUtil.JWT_ALGORITHM).build();
        return token -> {
            try {
                return jwtDecoder.decode(token);
            } catch (Exception e) {
                System.out.println(">>> JWT error: " + e.getMessage());
                throw e;
            }
        };
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public SecretKey getSecretKey() {
        byte[] keyBytes = Base64.from(jwtKey).decode();
        return new SecretKeySpec(keyBytes, 0, keyBytes.length,
                SecurityUtil.JWT_ALGORITHM.getName());
    }
    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(getSecretKey()));
    }

}
