package com.example.moneymanager.dto;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ProfileDtoResponse {
    private Long id;
    private String fullname;
    private String email;
    private String profileImgUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
