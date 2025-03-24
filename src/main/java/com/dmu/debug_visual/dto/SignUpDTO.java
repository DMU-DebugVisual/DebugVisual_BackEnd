package com.dmu.debug_visual.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpDTO {
    private String userId;
    private String email;
    private String password;
    private String name;
    private String profileInfo;
}
