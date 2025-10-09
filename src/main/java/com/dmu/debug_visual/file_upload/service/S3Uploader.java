package com.dmu.debug_visual.file_upload.service;

import com.dmu.debug_visual.file_upload.S3FileDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
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

    /**
     * 특정 사용자의 모든 파일 목록을 S3에서 조회합니다.
     * @param userId 조회할 사용자의 ID
     * @return 파일 정보 DTO 리스트
     */
    public List<S3FileDTO> listFiles(String userId) {
        // 1. 조회할 폴더(prefix)를 지정합니다. (예: "test-codes/")
        String prefix = userId + "-codes/";

        // 2. S3 객체 목록을 요청하기 위한 객체를 생성합니다.
        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(prefix)
                .build();

        // 3. S3 클라이언트로 객체 목록을 조회합니다.
        ListObjectsV2Response response = s3Client.listObjectsV2(listObjectsV2Request);
        List<S3Object> s3Objects = response.contents();

        // 4. 조회된 S3Object 목록을 우리가 만든 S3FileDTO 리스트로 변환합니다.
        return s3Objects.stream()
                .map(s3Object -> {
                    String fullKey = s3Object.key();
                    // "test-codes/UUID_원본파일이름" 에서 "원본파일이름"만 추출
                    String originalFileName = fullKey.substring(fullKey.indexOf('_') + 1);

                    return S3FileDTO.builder()
                            .fileName(originalFileName)
                            .fileUrl(s3Client.utilities().getUrl(builder -> builder.bucket(bucket).key(fullKey)).toString())
                            .uploadDate(LocalDateTime.ofInstant(s3Object.lastModified(), ZoneId.systemDefault()))
                            .fileSize(s3Object.size())
                            .build();
                })
                .collect(Collectors.toList());
    }
}