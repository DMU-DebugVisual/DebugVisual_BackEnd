package com.dmu.debug_visual.admin;

import com.dmu.debug_visual.user.dto.UserResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "관리자 API", description = "관리자용 사용자 관리 API")
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "전체 사용자 조회", description = "모든 사용자 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "전체 사용자 조회 성공")
    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @Operation(summary = "특정 사용자 조회", description = "userId로 특정 사용자를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용자 조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public ResponseEntity<?> getUser(@PathVariable String userId) {
        return adminService.getUserByUserId(userId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("해당 사용자를 찾을 수 없습니다."));
    }


    @Operation(summary = "활성 사용자 조회", description = "비활성화된 계정을 제외한 사용자 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "활성 사용자 조회 성공")
    @GetMapping("/users/active")
    public ResponseEntity<List<UserResponseDTO>> getActiveUsers() {
        return ResponseEntity.ok(adminService.getActiveUsers());
    }

    @Operation(summary = "사용자 추가", description = "관리자가 사용자를 등록합니다.")
    @ApiResponse(responseCode = "200", description = "사용자 등록 성공")
    @PostMapping("/users")
    public ResponseEntity<String> createUser(@RequestBody AdminUserCreateDTO dto) {
        UserResponseDTO created = adminService.registerUserByAdmin(dto);
        return ResponseEntity.ok("사용자 " + created.getUserId() + " 등록 완료");
    }

    @Operation(summary = "사용자 삭제", description = "사용자를 DB에서 완전히 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용자 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "삭제할 사용자를 찾을 수 없음")
    })
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable String userId) {
        boolean result = adminService.deleteUser(userId);
        return result
                ? ResponseEntity.ok("사용자 " + userId + " 삭제 완료")
                : ResponseEntity.status(404).body("삭제할 사용자를 찾을 수 없습니다.");
    }

    @Operation(summary = "비활성화된 사용자 복구", description = "비활성화된 사용자의 계정을 복구합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용자 복구 성공"),
            @ApiResponse(responseCode = "404", description = "복구할 사용자를 찾을 수 없음")
    })
    @PutMapping("/users/{userId}/activate")
    public ResponseEntity<String> activateUser(@PathVariable String userId) {
        boolean activated = adminService.activateUser(userId);
        return activated
                ? ResponseEntity.ok("사용자 " + userId + " 복구 완료")
                : ResponseEntity.status(404).body("복구할 사용자를 찾을 수 없습니다.");
    }
}
