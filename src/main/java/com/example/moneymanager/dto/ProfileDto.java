package com.example.moneymanager.dto;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProfileDto {
    private Long id;
    private String fullname;
    private String email;
    private String password;
    private String profileImgUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
