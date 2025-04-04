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
    private String userId; // 유저 ID (UK, AUTO_INCREMENT)

    @Column(length = 100, nullable = false)
    private String email; // 이메일 주소

    @Column(length = 255, nullable = false)
    private String password; // 비밀번호

    @Column(length = 50, nullable = false)
    private String name; // 사용자 이름

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Role role;

    @Column(length = 36, unique = true)
    private String connectId; // 웹소켓 연결용 고유 ID

    @Builder.Default
    @Column
    private Boolean isActive = true; // 계정 활성화 상태

    @Builder.Default
    @Column(updatable = false)
    private LocalDateTime joinDate = LocalDateTime.now(); // 가입 일자

    public enum Role {
        USER, ADMIN
    }

}
