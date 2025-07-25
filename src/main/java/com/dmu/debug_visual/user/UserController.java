package com.dmu.debug_visual.user;

import com.dmu.debug_visual.user.dto.LoginRequestDTO;
import com.dmu.debug_visual.user.dto.LoginResponseDTO;
import com.dmu.debug_visual.user.dto.SignUpDTO;
import com.dmu.debug_visual.user.dto.UserResponseDTO;
import com.dmu.debug_visual.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "사용자 관리 API", description = "사용자 관련 CRUD 및 인증 기능을 제공하는 컨트롤러")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "사용자가 회원가입을 합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 오류 또는 중복 ID/이메일")
    })
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody SignUpDTO dto) {
        try {
            UserResponseDTO user = userService.registerUser(dto);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "로그인", description = "사용자가 userId와 비밀번호로 로그인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "아이디 또는 비밀번호 불일치")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO dto) {
        LoginResponseDTO response = userService.login(dto);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @Operation(summary = "본인 정보 조회", description = "본인의 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 사용자가 존재하지 않음")
    })
    @GetMapping("/my/{userId}")
    public ResponseEntity<UserResponseDTO> getMyInfo(@PathVariable String userId) {
        return userService.getUserByUserId(userId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다."));
    }



    @Operation(summary = "회원 탈퇴", description = "계정을 비활성화합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "계정 비활성화 성공"),
            @ApiResponse(responseCode = "404", description = "비활성화할 사용자가 존재하지 않음")
    })
    @PutMapping("/{userId}/deactivate")
    public ResponseEntity<?> deactivateAccount(@PathVariable String userId) {
        try {
            userService.deactivateUser(userId);
            return ResponseEntity.ok("계정이 비활성화되었습니다.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 사용자를 찾을 수 없습니다.");
        }
    }

}
