package com.dmu.debug_visual.file_upload;

import com.dmu.debug_visual.file_upload.service.S3Uploader;
import com.dmu.debug_visual.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "파일 관리 API", description = "S3 파일 업로드 및 사용자별 파일 목록 조회를 제공하는 컨트롤러")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class FileController {

    private final S3Uploader s3Uploader;

    @Operation(summary = "파일 업로드", description = "form-data 형식으로 단일 파일을 AWS S3에 업로드하고, 저장된 파일의 URL을 반환합니다. JWT 인증이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업로드 성공",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "https://your-bucket.s3.amazonaws.com/.../file.txt"))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (JWT 토큰 누락 또는 유효하지 않음)",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 (파일 업로드 실패)",
                    content = @Content)
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String userId = userDetails.getUsername();

        try {
            String fileUrl = s3Uploader.upload(file, userId + "-codes");
            return ResponseEntity.ok(fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 업로드에 실패했습니다.");
        }
    }

    @Operation(summary = "사용자 파일 목록 조회", description = "특정 사용자가 업로드한 모든 파일의 목록을 조회합니다. 본인의 파일 목록만 조회할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = S3FileDTO.class)))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (JWT 토큰 누락 또는 유효하지 않음)",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음 (다른 사용자의 파일 목록에 접근 시도)",
                    content = @Content)
    })
    @GetMapping("/my")
    public ResponseEntity<List<S3FileDTO>> listFilesForUser(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String userId = userDetails.getUsername();

        List<S3FileDTO> files = s3Uploader.listFiles(userId);
        return ResponseEntity.ok(files);
    }
}