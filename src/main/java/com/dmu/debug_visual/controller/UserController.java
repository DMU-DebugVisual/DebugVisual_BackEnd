package com.dmu.debug_visual.controller;

import com.dmu.debug_visual.dto.LoginRequestDTO;
import com.dmu.debug_visual.dto.LoginResponseDTO;
import com.dmu.debug_visual.dto.SignUpDTO;
import com.dmu.debug_visual.dto.UserResponseDTO;
import com.dmu.debug_visual.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "사용자가 회원가입을 합니다.")
    @PostMapping("/signup")
    public ResponseEntity<UserResponseDTO> signUp(@RequestBody SignUpDTO dto) {
        UserResponseDTO user = userService.registerUser(dto);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "로그인", description = "userId와 비밀번호로 로그인합니다.")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO dto) {
        LoginResponseDTO response = userService.login(dto);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }


    @Operation(summary = "유저 정보 조회", description = "userId를 통해 해당 유저의 정보를 조회합니다.")
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUser(@PathVariable String userId) {
        return userService.getUserByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
