package com.dmu.debug_visual.dto;

import lombok.Data;

@Data
public class AdminUserCreateDTO {
    private String userId;
    private String email;
    private String password;
    private String name;
    private String role; // 관리자만 이 필드를 쓸 수 있음
}

