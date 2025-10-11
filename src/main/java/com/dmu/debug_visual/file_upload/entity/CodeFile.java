package com.dmu.debug_visual.file_upload.entity;

import com.dmu.debug_visual.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CodeFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String fileUUID; // 프론트와 통신할 때 사용할 고유 ID

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String s3FilePath; // S3에 저장된 실제 경로

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public CodeFile(String originalFileName, String s3FilePath, User user) {
        this.fileUUID = UUID.randomUUID().toString();
        this.originalFileName = originalFileName;
        this.s3FilePath = s3FilePath;
        this.user = user;
    }
}