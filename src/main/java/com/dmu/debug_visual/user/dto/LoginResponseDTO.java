package com.dmu.debug_visual.user.dto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponseDTO {
    private String userId;
    private String name;
    private String role;
    private boolean success;
    private String token;
    private String message;
}

