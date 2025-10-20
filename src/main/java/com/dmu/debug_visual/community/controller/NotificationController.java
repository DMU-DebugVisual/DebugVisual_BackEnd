package com.dmu.debug_visual.community.controller;

import com.dmu.debug_visual.community.dto.NotificationResponse;
import com.dmu.debug_visual.community.service.NotificationService;
import com.dmu.debug_visual.security.CustomUserDetails;
import com.dmu.debug_visual.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity; // ResponseEntity 사용
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 사용자 알림 관련 API 요청을 처리하는 컨트롤러 클래스.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "알림 API", description = "사용자 알림 조회 및 읽음 처리 API") // 태그 이름에 이모지 추가
@SecurityRequirement(name = "bearerAuth") // 모든 API에 JWT 인증 필요 명시
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 현재 로그인한 사용자의 모든 알림 목록을 조회합니다.
     *
     * @param userDetails 현재 로그인한 사용자의 정보 (JWT 토큰에서 추출)
     * @return 알림 DTO 리스트 (최신순 정렬)
     */
    @GetMapping
    @Operation(summary = "내 알림 목록 조회", description = "현재 로그인된 사용자의 모든 알림을 최신순으로 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = NotificationResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content)
    })
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
            @Parameter(hidden = true) // Swagger UI에서 파라미터 숨김 처리
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User user = userDetails.getUser();
        List<NotificationResponse> notifications = notificationService.getUserNotifications(user);
        return ResponseEntity.ok(notifications); // ResponseEntity로 감싸서 반환
    }

    /**
     * 특정 알림을 읽음 상태로 변경합니다.
     *
     * @param id          읽음 처리할 알림의 ID
     * @param userDetails 현재 로그인한 사용자의 정보 (JWT 토큰에서 추출)
     */
    @PutMapping("/{id}/read")
    @Operation(summary = "알림 읽음 처리", description = "특정 알림 ID를 받아 해당 알림을 읽음 상태로 변경합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "읽음 처리 성공", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음 (본인 알림 아님)", content = @Content),
            @ApiResponse(responseCode = "404", description = "해당 ID의 알림 없음", content = @Content)
    })
    public ResponseEntity<Void> markAsRead( // 반환 타입 void 대신 ResponseEntity<Void> 사용
                                            @Parameter(description = "읽음 처리할 알림 ID", required = true, example = "1")
                                            @PathVariable Long id,
                                            @Parameter(hidden = true) // Swagger UI에서 파라미터 숨김 처리
                                            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User user = userDetails.getUser();
        notificationService.markAsRead(id, user);
        return ResponseEntity.ok().build(); // 성공 시 200 OK만 반환
    }
}