package com.dmu.debug_visual.community.service;

import com.dmu.debug_visual.community.dto.NotificationResponse;
import com.dmu.debug_visual.community.entity.Notification;
import com.dmu.debug_visual.community.repository.NotificationRepository;
import com.dmu.debug_visual.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 사용자 알림 관련 비즈니스 로직을 처리하는 서비스 클래스.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // --- Public Methods ---

    /**
     * 일반 알림(게시물 ID 없음, 예: 좋아요)을 생성하고 저장합니다.
     *
     * @param receiver 알림을 받을 사용자
     * @param message  알림 내용
     */
    @Transactional
    public void notify(User receiver, String message) {
        createAndSaveNotification(receiver, message, null, Notification.NotificationType.LIKE);
    }

    /**
     * 게시물 관련 알림(예: 댓글)을 생성하고 저장합니다.
     *
     * @param receiver 알림을 받을 사용자
     * @param message  알림 내용
     * @param postId   관련된 게시물의 ID
     */
    @Transactional
    public void notify(User receiver, String message, Long postId) {
        createAndSaveNotification(receiver, message, postId, Notification.NotificationType.COMMENT);
    }

    /**
     * 특정 사용자의 모든 알림 목록을 DTO 리스트 형태로 조회합니다.
     *
     * @param user 알림 목록을 조회할 사용자
     * @return 알림 DTO 리스트 (최신순 정렬)
     */
    public List<NotificationResponse> getUserNotifications(User user) {
        List<Notification> notifications = notificationRepository.findByReceiverOrderByCreatedAtDesc(user);
        return notifications.stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 알림을 읽음 상태로 변경합니다.
     *
     * @param notificationId 읽음 처리할 알림의 ID
     * @param user           현재 로그인한 사용자 (권한 확인용)
     */
    @Transactional
    public void markAsRead(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("알림 없음 ID: " + notificationId));

        // 알림 수신자와 현재 사용자가 일치하는지 확인
        if (!notification.getReceiver().getUserNum().equals(user.getUserNum())) {
            throw new RuntimeException("알림 읽기 권한 없음 (알림 ID: " + notificationId + ")");
        }

        notification.markAsRead();
        // @Transactional 환경에서는 명시적인 save 호출 없이 더티 체킹으로 업데이트 가능
        // notificationRepository.save(notification);
    }

    // --- Private Helper Methods ---

    /**
     * Notification 엔티티를 생성하고 데이터베이스에 저장합니다.
     *
     * @param receiver 알림을 받을 사용자
     * @param message  알림 내용
     * @param postId   관련된 게시물의 ID (nullable)
     * @param type     알림 유형 (COMMENT, LIKE 등)
     */
    private void createAndSaveNotification(User receiver, String message, Long postId, Notification.NotificationType type) {
        Notification notification = Notification.builder()
                .receiver(receiver)
                .message(message)
                .notificationType(type)
                .postId(postId)
                // isRead는 @Builder.Default로 false가 기본값이므로 생략 가능
                .build();
        notificationRepository.save(notification);
    }
}