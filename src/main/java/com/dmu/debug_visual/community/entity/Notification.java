package com.dmu.debug_visual.community.entity;

import com.dmu.debug_visual.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter // 참고: 엔티티에 @Setter를 사용하는 것은 객체의 상태를 쉽게 변경할 수 있어 위험할 수 있습니다. 가능하면 markAsRead() 같은 명확한 메소드를 사용하는 것이 좋습니다.
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver_id") // 외래키 컬럼 이름을 명시적으로 지정해주는 것이 좋습니다.
    private User receiver;

    @Column(nullable = false)
    private String message;

    @Builder.Default
    @Column(nullable = false)
    private boolean isRead = false;

    private LocalDateTime createdAt;

    // ✨ [추가] 알림 타입을 구분하기 위한 필드 (예: 댓글, 좋아요)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;

    public enum NotificationType {
        COMMENT, // 댓글
        LIKE    // 좋아요
    }

    // ✨ [추가] 알림을 클릭했을 때 이동할 게시물의 ID
    @Column
    private Long postId;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // 읽음 처리 편의 메소드
    public void markAsRead() {
        this.isRead = true;
    }
}
