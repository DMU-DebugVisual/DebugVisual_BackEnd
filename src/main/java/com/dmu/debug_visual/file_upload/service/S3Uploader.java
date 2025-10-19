package com.dmu.debug_visual.file_upload.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * AWS S3와의 직접적인 통신(업로드, 조회, 삭제)을 담당하는 서비스 클래스.
 * 이 클래스는 비즈니스 로직을 포함하지 않고, 순수하게 S3 작업만 처리합니다.
 */
@Service
@RequiredArgsConstructor
public class S3Uploader {

    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    // =================================================================================
    // == 1. 파일 생성 및 수정 (Create & Update)
    // =================================================================================

    /**
     * S3에 파일을 업로드(또는 덮어쓰기)하고 URL을 반환합니다.
     * @param file 업로드할 파일
     * @param s3FilePath S3에 저장될 경로 (key)
     * @return 업로드된 파일의 S3 URL
     */
    public String upload(MultipartFile file, String s3FilePath) throws IOException {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(s3FilePath)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return s3Client.utilities().getUrl(builder -> builder.bucket(bucket).key(s3FilePath)).toString();
    }

    // =================================================================================
    // == 2. 파일 조회 (Read)
    // =================================================================================

    /**
     * S3에서 특정 파일의 내용을 문자열로 읽어옵니다.
     * @param s3FilePath 조회할 파일의 S3 경로 (key)
     * @return 파일 내용 (UTF-8 문자열)
     */
    public String getFileContent(String s3FilePath) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(s3FilePath)
                .build();

        ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
        return objectBytes.asString(StandardCharsets.UTF_8);
    }

    // =================================================================================
    // == 3. 파일 삭제 (Delete)
    // =================================================================================

    /**
     * S3에서 특정 파일을 삭제합니다.
     * @param s3FilePath 삭제할 파일의 S3 경로 (key)
     */
    public void deleteFile(String s3FilePath) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(s3FilePath)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }
}

