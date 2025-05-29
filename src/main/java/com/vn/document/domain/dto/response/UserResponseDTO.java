package com.vn.document.domain.dto.response;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDTO {
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String gender;
    private String role; // STUDENT | LECTURER | ADMIN
    private String status;
    private String avatarUrl;
}
