package com.dmu.debug_visual.community.controller;

import com.dmu.debug_visual.community.entity.Notification;
import com.dmu.debug_visual.community.service.NotificationService;
import com.dmu.debug_visual.security.CustomUserDetails;
import com.dmu.debug_visual.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "알림 API", description = "사용자에게 전달되는 알림 API")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "내 알림 목록 조회")
    public List<Notification> getMyNotifications(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        return notificationService.getUserNotifications(user);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "알림 읽음 처리")
    public void markAsRead(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        notificationService.markAsRead(id, user);
    }

}
