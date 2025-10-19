package com.dmu.debug_visual.community.dto;

import com.dmu.debug_visual.community.entity.Notification;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 알림 정보를 프론트엔드에 전달하기 위한 DTO
 */
@Getter
@Builder
public class NotificationResponse {
    private Long notificationId;
    private String message;
    private boolean isRead;
    private Notification.NotificationType notificationType;
    private LocalDateTime createdAt;
    private Long postId;

    /**
     * Notification 엔티티를 NotificationResponse DTO로 변환하는 정적 팩토리 메소드
     */
    public static NotificationResponse fromEntity(Notification notification) {
        return NotificationResponse.builder()
                .notificationId(notification.getId())
                .message(notification.getMessage())
                .isRead(notification.isRead())
                .notificationType(notification.getNotificationType())
                .createdAt(notification.getCreatedAt())
                .postId(notification.getPostId())
                .build();
    }
}
