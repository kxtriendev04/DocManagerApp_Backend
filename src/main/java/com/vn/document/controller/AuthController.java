package com.vn.document.controller;

import com.vn.document.domain.User;
import com.vn.document.domain.dto.request.LoginRequestDTO;
import com.vn.document.domain.dto.response.LoginResponseDTO;
import com.vn.document.service.UserService;
import com.vn.document.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthController {
    // dependency injection
    @Autowired
    private AuthenticationManagerBuilder authenticationManagerBuilder;
    @Autowired
    private SecurityUtil securityUtil;
    @Autowired
    private UserService userService;

    @Value("${jwt.refresh-token-validity-in-seconds}")
    private long jwtRefreshTokenExpri;

    public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> getMethodName(@RequestBody LoginRequestDTO loginRequestDTO) {
        if (loginRequestDTO.getUsername() == null || loginRequestDTO.getPassword() == null) {
            throw new RuntimeException("Tên đăng nhập và mật khẩu không được để trống!");
        }

        //Nạp input gồm username/password vào Security
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginRequestDTO.getUsername(), loginRequestDTO.getPassword());

        // Lưu người dùng trong Memory
        //xác thực người dùng => cần viết hàm loadUserByUsername, kế thừa từ: UserDetailsService
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        //nạp thông tin (nếu xử lý thành công) vào SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Tạo token và cookie
        return this.securityUtil.handleCreateTokenAndCookie(
                loginRequestDTO.getUsername());

    }

    @GetMapping("/account")
    public ResponseEntity<User> getAccount() {

        String email = securityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get() : "";
        User currentUser = userService.handleGetUserByUsername(email);
        return ResponseEntity.ok(currentUser);
    }

    // read cookie
    @GetMapping("refresh")
    public ResponseEntity<LoginResponseDTO> getRefresh(
            // get token from browser
            @CookieValue(name = "refresh_token", defaultValue = "default") String refreshToken
    ) {
        if (refreshToken.equals("default"))
            throw new RuntimeException("Vui lòng đăng nhập lại!!!");
        // check valid
        Jwt jwt = this.securityUtil.checkValidRefreshToken(refreshToken);
        String email = jwt.getSubject();
        // check user
//        User currentUser = userService.getUserByRefreshTokenAndEmail(refreshToken, email);

//        LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
//        loginResponseDTO.setUser(new LoginResponseDTO.UserLogin(
//                currentUser.getId(),
//                currentUser.getEmail(),
//                currentUser.getFullName()));
        return securityUtil.handleCreateTokenAndCookie(email);
    }

    @GetMapping("/logout")
    public ResponseEntity<Void> logout() {
        String email = securityUtil.getCurrentUserLogin().isPresent() ?
                securityUtil.getCurrentUserLogin().get() : "";
        if (email.equals(""))
            throw new RuntimeException("Token không hợp lệ");
        userService.handleUpdateRefreshToken(null, email);
        // remove cookie
        return ResponseEntity
                .ok()
                .body(null);
    }


}
