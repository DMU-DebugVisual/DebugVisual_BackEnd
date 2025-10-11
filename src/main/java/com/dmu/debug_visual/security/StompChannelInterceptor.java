package com.dmu.debug_visual.security;

import com.dmu.debug_visual.user.User;
import com.dmu.debug_visual.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import util.JwtTokenProvider;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            try {
                String token = Objects.requireNonNull(accessor.getFirstNativeHeader("Authorization")).substring(7);

                if (jwtTokenProvider.validateToken(token)) {
                    String userId = jwtTokenProvider.getUserIdFromToken(token);
                    User user = userRepository.findByUserId(userId)
                            .orElseThrow(() -> new RuntimeException("User not found"));

                    // ✨ 1. CustomUserDetails 객체를 생성합니다. (REST API와 동일한 방식)
                    CustomUserDetails userDetails = new CustomUserDetails(user);

                    // ✨ 2. CustomUserDetails를 기반으로 Authentication 객체를 생성합니다.
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

                    // ✨ 3. 웹소켓 세션에 최종 인증 정보를 등록합니다.
                    accessor.setUser(authentication);
                    log.info("STOMP User authenticated and set: {}", userDetails.getUsername());
                }
            } catch (Exception e) {
                log.error("STOMP connection authentication failed: {}", e.getMessage());
                return null;
            }
        }
        return message;
    }
}