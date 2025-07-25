package com.dmu.debug_visual.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponseDTO {
    private Long userNum;
    private String userId;
    private String email;
    private String name;
    private String role;
    private Boolean isActive;
    private LocalDateTime joinDate;
}
