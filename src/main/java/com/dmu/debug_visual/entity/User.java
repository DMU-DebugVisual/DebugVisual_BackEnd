package com.dmu.debug_visual.entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userNum; // 유저 고유 번호 (PK)

    @Column(nullable = false, unique = true)
    private Integer userId; // 유저 ID (UK, AUTO_INCREMENT)

    @Column(length = 100, nullable = false)
    private String email; // 이메일 주소

    @Column(length = 255, nullable = false)
    private String passwordHash; // 비밀번호

    @Column(length = 50, nullable = false)
    private String name; // 사용자 이름

    @Column(length = 20)
    private String role = "USER"; // 사용자 권한 (기본값: USER)

    @Column(columnDefinition = "TEXT")
    private String profileInfo; // 프로필 정보

    @Column(length = 36, unique = true)
    private String connectId; // 웹소켓 연결용 고유 ID

    @Column
    private Boolean isActive = true; // 계정 활성화 상태

    @Column(updatable = false)
    private LocalDateTime joinDate = LocalDateTime.now(); // 가입 일자
}
