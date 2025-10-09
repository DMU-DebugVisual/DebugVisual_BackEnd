package com.dmu.debug_visual.file_upload;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Uploader {

    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    public String upload(MultipartFile file, String dirName) throws IOException {
        // 1. 고유한 파일 이름 생성
        String originalFilename = file.getOriginalFilename();
        String uniqueFileName = dirName + "/" + UUID.randomUUID().toString() + "_" + originalFilename;

        // 2. S3에 업로드할 요청 객체 생성
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(uniqueFileName)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

        // 3. 파일의 InputStream을 RequestBody로 만들어 S3에 업로드
        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        // 4. 업로드된 파일의 URL 반환
        return s3Client.utilities().getUrl(builder -> builder.bucket(bucket).key(uniqueFileName)).toString();
    }
}