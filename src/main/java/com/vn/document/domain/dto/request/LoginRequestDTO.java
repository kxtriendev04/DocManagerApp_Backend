package com.vn.document.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDTO {
     @NotBlank(message = "Email không được để trống")
    private String username;
     @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
}
