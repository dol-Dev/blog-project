package com.doldev.dollog.domain.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.doldev.dollog.domain.chat.interceptor.StompHandler;

import lombok.RequiredArgsConstructor;

@SuppressWarnings("null")
@Configuration
@EnableWebSocketMessageBroker // 메세지 브로커(스톰프 메세지 환경 설정 o)
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompHandler stompHandler;

    // (1) Stomp 엔드포인트 등록 (메세지 구독/발행을 위해선 웹소캣 경로를 설정해야됨)
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("http://localhost:3000")
                .withSockJS();
    }

    // (2) 메시지 라우팅 설정 (WebSocket연결 후 WebSocket의 MessageBorker을 통해 구독/발행을 함)
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic"); // 메세지 구독을 위한 경로 설정
        registry.setApplicationDestinationPrefixes("/app"); // 메세지 발행을 위한 경로 설정
    }

    // 클라이언트로부터 서버로 들어오는 메시지를 처리하기 전에 intercept
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompHandler);
    }
}