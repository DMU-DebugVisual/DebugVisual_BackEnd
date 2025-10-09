package com.dmu.debug_visual.config;

import com.dmu.debug_visual.security.StompChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // WebSocket 메시지 브로커 기능 활성화
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompChannelInterceptor stompChannelInterceptor; // 추가

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 클라이언트가 메시지를 보내는 채널에 인터셉터를 등록합니다.
        registration.interceptors(stompChannelInterceptor);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 클라이언트가 WebSocket 연결을 시작할 엔드포인트를 설정합니다.
        // SockJS는 WebSocket을 지원하지 않는 브라우저를 위한 호환성 옵션입니다.
        registry.addEndpoint("/ws-collab").setAllowedOriginPatterns("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // "/topic"으로 시작하는 주소를 구독하는 클라이언트들에게 메시지를 브로드캐스팅(전파)합니다.
        registry.enableSimpleBroker("/topic");

        // "/app"으로 시작하는 주소로 들어온 메시지는 @MessageMapping이 붙은 메서드로 라우팅됩니다.
        registry.setApplicationDestinationPrefixes("/app");
    }
}