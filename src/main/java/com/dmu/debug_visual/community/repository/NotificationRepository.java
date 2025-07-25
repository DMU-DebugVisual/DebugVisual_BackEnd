package com.dmu.debug_visual.community.repository;

import com.dmu.debug_visual.community.entity.Notification;
import com.dmu.debug_visual.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByReceiverOrderByCreatedAtDesc(User receiver);
}
