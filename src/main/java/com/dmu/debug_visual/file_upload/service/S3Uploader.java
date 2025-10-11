package com.dmu.debug_visual.file_upload.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class S3Uploader {

    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * S3에 파일을 업로드(또는 덮어쓰기)하고 URL을 반환합니다.
     * 이제 이 메소드는 파일 경로를 직접 만들지 않고, 파라미터로 받습니다.
     */
    public String upload(MultipartFile file, String s3FilePath) throws IOException {
        // 1. S3에 업로드할 요청 객체 생성 (전달받은 s3FilePath를 key로 사용)
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(s3FilePath) // UUID로 새로 만드는 대신, 전달받은 경로를 그대로 사용
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

        // 2. 파일의 InputStream을 RequestBody로 만들어 S3에 업로드
        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        // 3. 업로드된 파일의 URL 반환
        return s3Client.utilities().getUrl(builder -> builder.bucket(bucket).key(s3FilePath)).toString();
    }
}