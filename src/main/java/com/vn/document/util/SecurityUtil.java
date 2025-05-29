package com.vn.document.util;

import com.nimbusds.jose.util.Base64;
import com.vn.document.domain.User;
import com.vn.document.domain.dto.response.LoginResponseDTO;
import com.vn.document.domain.dto.response.UserResponseToken;
import com.vn.document.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class SecurityUtil {
    @Autowired
    private JwtEncoder jwtEncoder;
    @Autowired
    private UserService userService;
    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS512;

    @Value("${jwt.base64-secret}")
    private String jwtKey;
    @Value("${jwt.access-token-validity-in-seconds}")
    private long jwtAccessTokenExpri;
    @Value("${jwt.refresh-token-validity-in-seconds}")
    private long jwtRefreshTokenExpri;

    public String createAccessToken(String email, LoginResponseDTO.UserLogin userLogin){
        Instant now = Instant.now();
        Instant validity = now.plus(this.jwtAccessTokenExpri, ChronoUnit.SECONDS);
        User user = userService.handleGetUserByUsername(userLogin.getEmail());
        UserResponseToken userResponseToken = new UserResponseToken(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().toString()
        );
        
        // @formatter:off
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(email)
                .claim("user", userResponseToken)
                .claim("roles", List.of(user.getRole().toString()))
                .build();
        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader,
                claims)).getTokenValue();
    }
    public String createRefreshToken(String email, LoginResponseDTO loginResponseDTO){
        Instant now = Instant.now();
        Instant validity = now.plus(this.jwtRefreshTokenExpri, ChronoUnit.SECONDS);
        // @formatter:off
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(email)
                .claim("user", loginResponseDTO.getUser())
                .build();
        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader,
                claims)).getTokenValue();
    }
    private SecretKey getSecretKey() {
        byte[] keyBytes = Base64.from(jwtKey).decode();
        return new SecretKeySpec(keyBytes, 0,
                keyBytes.length, JWT_ALGORITHM.getName());
    }
    /**
     * Get the login of the current user.
     *
     * @return the login of the current user.
     */
    public static Optional<String> getCurrentUserLogin() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(extractPrincipal(securityContext.getAuthentication()));
    }

    private static String extractPrincipal(Authentication authentication) {
        if (authentication == null) {
            return null;
        } else if (authentication.getPrincipal() instanceof UserDetails springSecurityUser) {
            return springSecurityUser.getUsername();
        } else if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        } else if (authentication.getPrincipal() instanceof String s) {
            return s;
        }
        return null;
    }

    /**
     * Get the JWT of the current user.
     *
     * @return the JWT of the current user.
     */
    public static Optional<String> getCurrentUserJWT() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(securityContext.getAuthentication())
                .filter(authentication -> authentication.getCredentials() instanceof String)
                .map(authentication -> (String) authentication.getCredentials());
    }

    public Jwt checkValidRefreshToken(String token){
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(
                getSecretKey()).macAlgorithm(JWT_ALGORITHM).build();
        try {
            return jwtDecoder.decode(token);
        } catch (Exception e) {
            System.out.println(">>> Refresh token error: " + e.getMessage());
            throw e;
        }
    }
    public ResponseEntity<LoginResponseDTO> handleCreateTokenAndCookie(String email) {
        LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
        User currentUser = userService.handleGetUserByUsername(email);

        // Tạo thông tin user
        LoginResponseDTO.UserLogin userLogin = new LoginResponseDTO.UserLogin(
                currentUser.getId(),
                currentUser.getEmail(),
                currentUser.getFullName()
        );
        loginResponseDTO.setUser(userLogin);

        // Tạo access token
        String accessToken = this.createAccessToken(email, loginResponseDTO.getUser());
        loginResponseDTO.setAccessToken(accessToken);

        // Tạo refresh token
        String refreshToken = this.createRefreshToken(email, loginResponseDTO);
        loginResponseDTO.setRefreshToken(refreshToken); // Thêm refresh token vào response

        // Cập nhật refresh token vào database
        userService.handleUpdateRefreshToken(refreshToken, email);

        return ResponseEntity.ok(loginResponseDTO);
    }

//    ????

    /**
     * Check if a user is authenticated.
     *
     * @return true if the user is authenticated, false otherwise.
     */
    // public static boolean isAuthenticated() {
    //     Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    //     return authentication != null && getAuthorities(authentication).noneMatch(AuthoritiesConstants.ANONYMOUS::equals);
    // }

    /**
     * Checks if the current user has any of the authorities.
     *
     * @param authorities the authorities to check.
     * @return true if the current user has any of the authorities, false otherwise.
     */
    // public static boolean hasCurrentUserAnyOfAuthorities(String... authorities) {
    //     Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    //     return (
    //         authentication != null && getAuthorities(authentication).anyMatch(authority -> Arrays.asList(authorities).contains(authority))
    //     );
    // }

    /**
     * Checks if the current user has none of the authorities.
     *
     * @param authorities the authorities to check.
     * @return true if the current user has none of the authorities, false otherwise.
     */
    // public static boolean hasCurrentUserNoneOfAuthorities(String... authorities) {
    //     return !hasCurrentUserAnyOfAuthorities(authorities);
    // }

    /**
     * Checks if the current user has a specific authority.
     *
     * @param authority the authority to check.
     * @return true if the current user has the authority, false otherwise.
     */


}
