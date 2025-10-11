package com.dmu.debug_visual.file_upload.service;

import com.dmu.debug_visual.file_upload.CodeFileRepository;
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

@Service
@RequiredArgsConstructor
public class FileService {

    private final S3Uploader s3Uploader; // 역할이 단순화된 S3Uploader 주입
    private final CodeFileRepository codeFileRepository;
    private final UserRepository userRepository;

    @Transactional
    public FileResponseDTO saveOrUpdateFile(String fileUUID, MultipartFile file, String userId) throws IOException {

        // 1. 요청 보낸 사용자의 엔티티를 조회합니다.
        User currentUser = userRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        // 2. fileUUID의 존재 여부로 '최초 저장'과 '수정'을 구분합니다.
        if (fileUUID == null || fileUUID.isBlank()) {

            // 💡 최초 저장 로직
            String originalFileName = file.getOriginalFilename();
            // S3에 저장될 고유한 경로 생성 (사용자별로 폴더를 분리하면 관리하기 좋습니다)
            String s3FilePath = "user-codes/" + currentUser.getUserId() + "/" + UUID.randomUUID().toString() + "_" + originalFileName;

            // S3Uploader를 통해 파일을 S3에 업로드합니다.
            String fileUrl = s3Uploader.upload(file, s3FilePath);

            // 파일 메타데이터를 DB(CodeFile 테이블)에 저장합니다.
            CodeFile newCodeFile = CodeFile.builder()
                    .originalFileName(originalFileName)
                    .s3FilePath(s3FilePath)
                    .user(currentUser)
                    .build();
            codeFileRepository.save(newCodeFile);

            // 프론트엔드에 새로 생성된 fileUUID와 파일 URL을 반환합니다.
            return new FileResponseDTO(newCodeFile.getFileUUID(), fileUrl);

        } else {

            // 💡 수정(덮어쓰기) 로직
            CodeFile existingCodeFile = codeFileRepository.findByFileUUID(fileUUID)
                    .orElseThrow(() -> new EntityNotFoundException("File not found with UUID: " + fileUUID));

            // (보안) 파일을 수정하려는 사용자가 실제 소유자인지 확인합니다.
            if (!existingCodeFile.getUser().getUserId().equals(currentUser.getUserId())) {
                throw new IllegalStateException("You do not have permission to modify this file.");
            }

            // S3Uploader에 "기존과 동일한 경로"를 전달하여 파일을 덮어쓰게 합니다.
            String fileUrl = s3Uploader.upload(file, existingCodeFile.getS3FilePath());

            // DB 정보는 그대로 유지합니다. (수정 시간이 필요하다면 엔티티에 필드 추가 후 갱신)

            // 프론트엔드에 기존 fileUUID와 갱신된 파일 URL을 반환합니다.
            return new FileResponseDTO(existingCodeFile.getFileUUID(), fileUrl);
        }
    }

    @Transactional(readOnly = true)
    public List<UserFileDTO> getUserFiles(String  userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        List<CodeFile> userCodeFiles = codeFileRepository.findByUser(user);

        return userCodeFiles.stream()
                .map(codeFile -> UserFileDTO.builder()
                        .fileUUID(codeFile.getFileUUID())
                        .originalFileName(codeFile.getOriginalFileName())
                        .build())
                .collect(Collectors.toList());
    }
}
