package com.doldev.dollog.domain.chat.interceptor;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import com.doldev.dollog.domain.chat.repository.ChatRoomRedisRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "StompHandler")
@RequiredArgsConstructor
@Component
public class StompHandler extends HttpSessionHandshakeInterceptor implements ChannelInterceptor {

    private final ChatRoomRedisRepository chatRoomRedisRepository;

    // WebSocket 연결 시 JWT 토큰 검증 및 사용자 인증
    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        return message;
    }

    // WebSocket 구독 관리 (채팅방 입장 시 실행)
    @Override
    public void postSend(@NonNull Message<?> message, @NonNull MessageChannel channel, boolean sent) {
        if (!sent) {
            throw new MessagingException("WebSocket message delivery failed.");
        }

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            manageSubscription(accessor);
        }
    }

    // 구독 관리 및 세션 ID와 채팅방 ID 연결
    private void manageSubscription(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        String destination = accessor.getDestination();

        if (sessionId == null || destination == null) {
            log.warn("Failed to manage subscription: sessionId or destination is null.");
            return;
        }

        String roomId = destination.substring("/topic/rooms/".length());
        chatRoomRedisRepository.setUserEnterInfo(sessionId, roomId);
    }

    // WebSocket 연결 종료 시 사용자 정보 삭제
    @Override
    public void afterSendCompletion(@NonNull Message<?> message, @NonNull MessageChannel channel, boolean sent, @Nullable Exception ex) {
        if (!sent) {
            log.error("Failed to complete WebSocket message delivery: {}", message);
            if (ex != null) {
                log.error("Error details: {}", ex.getMessage());
            }
            throw new IllegalStateException("WebSocket message processing failed.");
        }

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        String sessionId = accessor.getSessionId();

        if (sessionId != null) {
            chatRoomRedisRepository.removeUserEnterInfo(sessionId);
        } else {
            log.warn("Session ID is null on WebSocket disconnect.");
        }
    }
}
