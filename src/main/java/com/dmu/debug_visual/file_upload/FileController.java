package com.dmu.debug_visual.file_upload;

import com.dmu.debug_visual.file_upload.dto.FileResponseDTO;
import com.dmu.debug_visual.file_upload.dto.UserFileDTO;
import com.dmu.debug_visual.file_upload.service.FileService;
import com.dmu.debug_visual.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "파일 관리 API", description = "S3 파일 생성, 수정(덮어쓰기) 및 사용자별 파일 목록 조회를 제공합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/file") // 경로를 단수로 통일
public class FileController {

    private final FileService fileService;

    @Operation(summary = "파일 저장 또는 수정 (덮어쓰기)",
            description = "form-data로 파일을 업로드합니다. `fileUUID` 파라미터 유무에 따라 동작이 달라집니다.\n\n" +
                    "- **`fileUUID`가 없으면**: 신규 파일로 저장하고 새로운 `fileUUID`를 발급합니다.\n" +
                    "- **`fileUUID`가 있으면**: 해당 `fileUUID`를 가진 기존 파일을 덮어씁니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "요청 성공 (생성 또는 수정 완료)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FileResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (JWT 토큰 누락 또는 유효하지 않음)", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음 (타인의 파일 수정을 시도)", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 `fileUUID`로 수정 요청", content = @Content)
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileResponseDTO> uploadOrUpdateFile(
            @Parameter(description = "업로드할 파일")
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "수정할 파일의 고유 ID. 신규 업로드 시에는 생략합니다.")
            @RequestParam(value = "fileUUID", required = false) String fileUUID,

            @AuthenticationPrincipal CustomUserDetails userDetails) throws IOException {

        // CustomUserDetails에서 사용자 ID (Long 타입)를 가져오는 메소드가 있다고 가정합니다.
        // 예: userDetails.getId()
        String currentUserId = userDetails.getUsername();

        FileResponseDTO fileResponse = fileService.saveOrUpdateFile(fileUUID, file, currentUserId);
        return ResponseEntity.ok(fileResponse);
    }

    @Operation(summary = "내 파일 목록 조회", description = "현재 로그인한 사용자가 생성한 모든 파일의 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserFileDTO.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (JWT 토큰 누락 또는 유효하지 않음)", content = @Content)
    })
    @GetMapping("/my")
    public ResponseEntity<List<UserFileDTO>> getMyFiles(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String currentUserId = userDetails.getUsername();
        List<UserFileDTO> myFiles = fileService.getUserFiles(currentUserId);
        return ResponseEntity.ok(myFiles);
    }
}