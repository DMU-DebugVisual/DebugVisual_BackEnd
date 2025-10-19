package com.dmu.debug_visual.file_upload;

import com.dmu.debug_visual.file_upload.dto.FileContentResponse;
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

@Tag(name = "파일 관리 API", description = "S3 파일 생성, 수정, 조회 및 삭제 기능을 제공합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/file")
public class FileController {

    private final FileService fileService;

    // =================================================================================
    // == 1. 파일 생성 및 수정 (Create & Update)
    // =================================================================================

    @Operation(summary = "파일 저장 또는 수정 (덮어쓰기)",
            description = "form-data로 파일을 업로드합니다. `fileUUID` 파라미터 유무에 따라 동작이 달라집니다.\n\n" +
                    "- **`fileUUID`가 없으면**: 신규 파일로 저장하고 새로운 `fileUUID`를 발급합니다.\n" +
                    "- **`fileUUID`가 있으면**: 해당 `fileUUID`를 가진 기존 파일을 덮어씁니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "요청 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FileResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 `fileUUID`로 수정 요청")
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileResponseDTO> uploadOrUpdateFile(
            @Parameter(description = "업로드할 파일") @RequestParam("file") MultipartFile file,
            @Parameter(description = "수정할 파일의 고유 ID. 신규 업로드 시에는 생략합니다.") @RequestParam(value = "fileUUID", required = false) String fileUUID,
            @AuthenticationPrincipal CustomUserDetails userDetails) throws IOException {

        String currentUserId = userDetails.getUsername();
        FileResponseDTO fileResponse = fileService.saveOrUpdateFile(fileUUID, file, currentUserId);
        return ResponseEntity.ok(fileResponse);
    }

    // =================================================================================
    // == 2. 파일 조회 (Read)
    // =================================================================================

    @Operation(summary = "내 파일 목록 조회", description = "현재 로그인한 사용자가 생성한 모든 파일의 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserFileDTO.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/my")
    public ResponseEntity<List<UserFileDTO>> getMyFiles(@AuthenticationPrincipal CustomUserDetails userDetails) {
        String currentUserId = userDetails.getUsername();
        List<UserFileDTO> myFiles = fileService.getUserFiles(currentUserId);
        return ResponseEntity.ok(myFiles);
    }

    @Operation(summary = "파일 내용 조회", description = "특정 파일의 내용을 조회합니다. 본인의 파일만 조회할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = FileContentResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음")
    })
    @GetMapping("/{fileUUID}/content")
    public ResponseEntity<FileContentResponse> getFileContent(
            @Parameter(description = "내용을 조회할 파일의 고유 ID") @PathVariable String fileUUID,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        FileContentResponse response = fileService.getFileContent(fileUUID, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    // =================================================================================
    // == 3. 파일 삭제 (Delete)
    // =================================================================================

    @Operation(summary = "파일 삭제", description = "특정 파일을 S3와 DB에서 모두 삭제합니다. 본인의 파일만 삭제할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음")
    })
    @DeleteMapping("/{fileUUID}")
    public ResponseEntity<Void> deleteFile(
            @Parameter(description = "삭제할 파일의 고유 ID") @PathVariable String fileUUID,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        fileService.deleteFile(fileUUID, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}

