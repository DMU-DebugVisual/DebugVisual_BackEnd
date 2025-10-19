package com.dmu.debug_visual.file_upload.service;

import com.dmu.debug_visual.file_upload.CodeFileRepository;
import com.dmu.debug_visual.file_upload.dto.FileContentResponse;
import com.dmu.debug_visual.file_upload.dto.FileResponseDTO;
import com.dmu.debug_visual.file_upload.dto.UserFileDTO;
import com.dmu.debug_visual.file_upload.entity.CodeFile;
import com.dmu.debug_visual.user.User;
import com.dmu.debug_visual.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 파일 관련 비즈니스 로직을 처리하는 서비스 클래스.
 * S3Uploader와 DB(CodeFileRepository)를 함께 사용하여 파일의 생성, 수정, 조회, 삭제를 관리합니다.
 */
@Service
@RequiredArgsConstructor
public class FileService {

    private final S3Uploader s3Uploader;
    private final CodeFileRepository codeFileRepository;
    private final UserRepository userRepository;

    // =================================================================================
    // == 1. 파일 생성 및 수정 (Create & Update)
    // =================================================================================

    /**
     * 파일을 새로 저장하거나 기존 파일을 덮어씁니다.
     * @param fileUUID 수정할 파일의 ID (신규 저장 시 null)
     * @param file 업로드된 파일 데이터
     * @param userId 요청을 보낸 사용자의 ID
     * @return 생성 또는 수정된 파일의 정보
     */
    @Transactional
    public FileResponseDTO saveOrUpdateFile(String fileUUID, MultipartFile file, String userId) throws IOException {
        User currentUser = userRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        if (fileUUID == null || fileUUID.isBlank()) {
            // --- 신규 파일 생성 ---
            String originalFileName = file.getOriginalFilename();
            String s3FilePath = "user-codes/" + currentUser.getUserId() + "/" + UUID.randomUUID().toString() + "_" + originalFileName;

            String fileUrl = s3Uploader.upload(file, s3FilePath);

            CodeFile newCodeFile = CodeFile.builder()
                    .originalFileName(originalFileName)
                    .s3FilePath(s3FilePath)
                    .user(currentUser)
                    .build();
            codeFileRepository.save(newCodeFile);

            return new FileResponseDTO(newCodeFile.getFileUUID(), fileUrl);

        } else {
            // --- 기존 파일 수정 ---
            CodeFile existingCodeFile = findAndVerifyOwner(fileUUID, userId);
            String fileUrl = s3Uploader.upload(file, existingCodeFile.getS3FilePath());

            return new FileResponseDTO(existingCodeFile.getFileUUID(), fileUrl);
        }
    }

    // =================================================================================
    // == 2. 파일 조회 (Read)
    // =================================================================================

    /**
     * 특정 사용자가 소유한 모든 파일의 목록을 조회합니다.
     * @param userId 조회할 사용자의 ID
     * @return 파일 목록 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<UserFileDTO> getUserFiles(String  userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        List<CodeFile> userCodeFiles = codeFileRepository.findByUser(user);

        return userCodeFiles.stream()
                .map(codeFile -> UserFileDTO.builder()
                        .fileUUID(codeFile.getFileUUID())
                        .originalFileName(codeFile.getOriginalFileName())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 특정 파일의 내용을 조회합니다.
     * @param fileUUID 조회할 파일의 ID
     * @param userId 요청을 보낸 사용자의 ID
     * @return 파일의 원본 이름과 내용이 담긴 DTO
     */
    @Transactional(readOnly = true)
    public FileContentResponse getFileContent(String fileUUID, String userId) {
        CodeFile codeFile = findAndVerifyOwner(fileUUID, userId);
        String content = s3Uploader.getFileContent(codeFile.getS3FilePath());

        return FileContentResponse.builder()
                .originalFileName(codeFile.getOriginalFileName())
                .content(content)
                .build();
    }

    // =================================================================================
    // == 3. 파일 삭제 (Delete)
    // =================================================================================

    /**
     * 특정 파일을 S3와 DB에서 모두 삭제합니다.
     * @param fileUUID 삭제할 파일의 ID
     * @param userId 요청을 보낸 사용자의 ID
     */
    @Transactional
    public void deleteFile(String fileUUID, String userId) {
        CodeFile codeFile = findAndVerifyOwner(fileUUID, userId);

        s3Uploader.deleteFile(codeFile.getS3FilePath());
        codeFileRepository.delete(codeFile);
    }

    // =================================================================================
    // == Private Helper Methods
    // =================================================================================

    /**
     * 파일 ID로 파일을 조회하고, 요청한 사용자가 파일의 소유주인지 검증하는 private 헬퍼 메소드
     * @param fileUUID 조회할 파일의 ID
     * @param userId 요청을 보낸 사용자의 ID
     * @return 검증된 CodeFile 엔티티
     */
    private CodeFile findAndVerifyOwner(String fileUUID, String userId) {
        CodeFile codeFile = codeFileRepository.findByFileUUID(fileUUID)
                .orElseThrow(() -> new EntityNotFoundException("File not found with UUID: " + fileUUID));

        if (!codeFile.getUser().getUserId().equals(userId)) {
            throw new IllegalStateException("You do not have permission to access this file.");
        }
        return codeFile;
    }
}

