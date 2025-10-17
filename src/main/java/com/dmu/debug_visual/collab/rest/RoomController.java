package com.dmu.debug_visual.collab.rest;

import com.dmu.debug_visual.collab.domain.entity.CodeSession.SessionStatus;
import com.dmu.debug_visual.security.CustomUserDetails;
import com.dmu.debug_visual.collab.rest.dto.CreateRoomRequest;
import com.dmu.debug_visual.collab.rest.dto.CreateSessionRequest;
import com.dmu.debug_visual.collab.rest.dto.RoomResponse;
import com.dmu.debug_visual.collab.rest.dto.SessionResponse;
import com.dmu.debug_visual.collab.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "협업 방 및 세션 관리 API", description = "실시간 협업을 위한 방 생성, 세션 관리, 권한 부여, 강퇴 등 모든 REST API를 제공합니다.")
@RestController
@RequestMapping("/api/collab")
@RequiredArgsConstructor
public class RoomController {

    // ✨ Controller는 이제 Service에만 의존합니다. 훨씬 깔끔해졌죠!
    private final RoomService roomService;

    // --- 1. 방 관리 ---
    @Operation(summary = "새로운 협업 방 생성", description = "DB에 새로운 협업 방을 생성하고, 방장을 첫 참여자로 자동 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "방 생성 성공", content = @Content(schema = @Schema(implementation = RoomResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    })
    @PostMapping("/rooms")
    public ResponseEntity<RoomResponse> createRoom(@RequestBody CreateRoomRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        String ownerUserId = userDetails.getUsername();
        RoomResponse response = roomService.createRoom(request, ownerUserId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "방에서 참가자 강퇴 (방장 전용)", description = "방장이 특정 참가자를 방에서 영구적으로 제외시킵니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "강퇴 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (방장이 아님)"),
            @ApiResponse(responseCode = "404", description = "방 또는 참가자를 찾을 수 없음")
    })
    @DeleteMapping("/rooms/{roomId}/participants/{targetUserId}")
    public ResponseEntity<Void> kickParticipant(
            @Parameter(description = "대상 방의 고유 ID") @PathVariable String roomId,
            @Parameter(description = "강퇴시킬 사용자의 ID") @PathVariable String targetUserId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        roomService.kickParticipant(roomId, userDetails.getUsername(), targetUserId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "협업 방에 참여자로 등록", description = "사용자가 특정 방에 참여자로 자신을 등록합니다. 웹소켓에 연결하기 전에 반드시 호출해야 합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "참여 등록 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 방")
    })
    @PostMapping("/rooms/{roomId}/participants")
    public ResponseEntity<Void> joinRoom(
            @Parameter(description = "참여할 방의 고유 ID") @PathVariable String roomId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        roomService.joinRoom(roomId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    // --- 2. 세션 관리 ---
    @Operation(summary = "방 안에 새 코드 세션 생성 (방송 시작)", description = "기존 방 안에 독립적인 새 코드 편집 세션을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "세션 생성 성공", content = @Content(schema = @Schema(implementation = SessionResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 방")
    })
    @PostMapping("/rooms/{roomId}/sessions")
    public ResponseEntity<SessionResponse> createSession(
            @Parameter(description = "세션을 생성할 방의 고유 ID") @PathVariable String roomId,
            @RequestBody CreateSessionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String creatorUserId = userDetails.getUsername();
        SessionResponse response = roomService.createCodeSessionInRoom(roomId, request, creatorUserId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "세션 상태 변경 (방송 켜기/끄기, 세션 생성자 전용)", description = "세션의 상태를 'ACTIVE' 또는 'INACTIVE'로 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상태 변경 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (세션 생성자가 아님)"),
            @ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음")
    })
    @PatchMapping("/sessions/{sessionId}/status")
    public ResponseEntity<Void> updateSessionStatus(
            @Parameter(description = "상태를 변경할 세션의 고유 ID") @PathVariable String sessionId,
            @RequestBody Map<String, SessionStatus> statusMap,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        roomService.updateSessionStatus(sessionId, userDetails.getUsername(), statusMap.get("status"));
        return ResponseEntity.ok().build();
    }

    // --- 3. 세션 권한 관리 ---
    @Operation(summary = "세션 내 쓰기 권한 부여 (세션 생성자 전용)", description = "세션 생성자가 특정 참가자에게 해당 세션의 쓰기 권한을 부여합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "권한 부여 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (세션 생성자가 아님)"),
            @ApiResponse(responseCode = "404", description = "세션 또는 참가자를 찾을 수 없음")
    })
    @PostMapping("/sessions/{sessionId}/permissions/{targetUserId}")
    public ResponseEntity<Void> grantWritePermission(
            @Parameter(description = "권한을 부여할 세션의 고유 ID") @PathVariable String sessionId,
            @Parameter(description = "권한을 부여받을 사용자의 ID") @PathVariable String targetUserId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        roomService.grantWritePermissionInSession(sessionId, userDetails.getUsername(), targetUserId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "세션 내 쓰기 권한 회수 (세션 생성자 전용)", description = "세션 생성자가 특정 참가자의 쓰기 권한을 회수합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "권한 회수 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (세션 생성자가 아님)"),
            @ApiResponse(responseCode = "404", description = "세션 또는 참가자를 찾을 수 없음")
    })
    @DeleteMapping("/sessions/{sessionId}/permissions/{targetUserId}")
    public ResponseEntity<Void> revokeWritePermission(
            @Parameter(description = "권한을 회수할 세션의 고유 ID") @PathVariable String sessionId,
            @Parameter(description = "권한을 회수당할 사용자의 ID") @PathVariable String targetUserId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        roomService.revokeWritePermissionInSession(sessionId, userDetails.getUsername(), targetUserId);
        return ResponseEntity.ok().build();
    }
}

