package com.dmu.debug_visual.community.service;

import com.dmu.debug_visual.community.entity.Notification;
import com.dmu.debug_visual.community.repository.NotificationRepository;
import com.dmu.debug_visual.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void notify(User receiver, String message) {
        Notification notification = Notification.builder()
                .receiver(receiver)
                .message(message)
                .build();
        notificationRepository.save(notification);
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
}
