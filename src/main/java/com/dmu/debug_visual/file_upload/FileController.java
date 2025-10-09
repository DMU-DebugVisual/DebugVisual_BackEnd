package com.dmu.debug_visual.file_upload;

import com.dmu.debug_visual.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files") // 공통되는 URL 경로 설정
public class FileController {

    private final S3Uploader s3Uploader;

    @Operation(summary = "파일 업로드", description = "form-data 형식으로 파일을 업로드합니다.")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails) { // <-- String 대신 CustomUserDetails로 변경

        // userDetails 객체에서 userId를 직접 꺼내서 사용합니다.
        String userId = userDetails.getUsername(); // 또는 userDetails.getUser().getUserId()

        try {
            String fileUrl = s3Uploader.upload(file, userId + "-codes");
            return ResponseEntity.ok(fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 업로드에 실패했습니다.");
        }
    }
}