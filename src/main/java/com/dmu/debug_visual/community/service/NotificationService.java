package com.dmu.debug_visual.community.service;

import com.dmu.debug_visual.community.entity.Notification;
import com.dmu.debug_visual.community.repository.NotificationRepository;
import com.dmu.debug_visual.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * 일반 알림을 생성합니다. (게시물 ID가 없는 경우)
     * @param receiver 알림을 받을 사용자
     * @param message  알림 내용
     */
    @Transactional
    public void notify(User receiver, String message) {
        // postId가 없는 경우, null로 저장합니다.
        // 좋아요 기능 등에서 이 메소드를 호출할 수 있습니다.
        createAndSaveNotification(receiver, message, null, Notification.NotificationType.LIKE); // 기본 타입을 LIKE 등으로 설정
    }

    /**
     * 게시물과 관련된 알림을 생성합니다. (댓글 등)
     * @param receiver 알림을 받을 사용자
     * @param message  알림 내용
     * @param postId   관련된 게시물의 ID
     */
    @Transactional
    public void notify(User receiver, String message, Long postId) {
        createAndSaveNotification(receiver, message, postId, Notification.NotificationType.COMMENT);
    }

    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByReceiverOrderByCreatedAtDesc(user);
    }

    public void markAsRead(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("알림 없음"));
        if (!notification.getReceiver().getUserNum().equals(user.getUserNum())) {
            throw new RuntimeException("권한 없음");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    /**
     * Notification 엔티티를 생성하고 저장하는 private 헬퍼 메소드
     */
    private void createAndSaveNotification(User receiver, String message, Long postId, Notification.NotificationType type) {
        Notification notification = Notification.builder()
                .receiver(receiver)
                .message(message)
                .notificationType(type)
                .postId(postId) // postId가 null이 아니면 저장, null이면 null로 저장
                .build();
        notificationRepository.save(notification);
    }
}
