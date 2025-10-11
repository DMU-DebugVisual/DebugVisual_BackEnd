package com.dmu.debug_visual.collab.rest;

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

@Tag(name = "협업 방 및 세션 관리 API", description = "실시간 협업을 위한 방과 코드 세션을 생성하고 관리합니다.")
@RestController
@RequestMapping("/api/collab-rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService; // DB 관련 서비스

    @Operation(summary = "새로운 협업 방 생성", description = "DB에 새로운 협업 방을 생성하고, 방장(owner)을 첫 참여자로 자동 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "방 생성 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RoomResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    })
    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(
            @RequestBody CreateRoomRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String ownerUserId = userDetails.getUsername();
        RoomResponse response = roomService.createRoom(request, ownerUserId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "방 안에 새 코드 세션 생성", description = "기존에 생성된 협업 방 안에 독립적인 새 코드 편집 세션을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "세션 생성 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SessionResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
            @ApiResponse(responseCode = "403", description = "해당 방의 참여자가 아님", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 방", content = @Content)
    })
    @PostMapping("/{roomId}/sessions")
    public ResponseEntity<SessionResponse> createSession(
            @Parameter(description = "세션을 생성할 방의 고유 ID (UUID)", required = true)
            @PathVariable String roomId,

            @RequestBody CreateSessionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String creatorUserId = userDetails.getUsername();
        SessionResponse response = roomService.createCodeSessionInRoom(roomId, request, creatorUserId);
        return ResponseEntity.ok(response);
    }
}