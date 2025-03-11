package com.doldev.dollog.domain.chat.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.doldev.dollog.domain.chat.application.ChatMessageService;
import com.doldev.dollog.domain.chat.dto.req.ChatMessageReqDto;
import com.doldev.dollog.domain.chat.dto.res.ChatMessageResDto;
import com.doldev.dollog.global.auth.principal.CustomUserDetails;
import com.doldev.dollog.global.dto.ApiResDto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class ChatMessageController {
    private final ChatMessageService chatMessageService;

    // 특정 사용자의 채팅방 메시지 조회
    @GetMapping("/api/chat/{roomId}/messages")
    public ResponseEntity<ApiResDto<?>> getChatRoomMessages(
            @PathVariable int roomId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<ChatMessageResDto> messages = chatMessageService.getChatRoomChatMessages(roomId, userDetails);
        return ResponseEntity.ok()
                .body(ApiResDto.builder().messageCode("채팅방 채팅 기록 조회 완료").data(messages).build());
    }

    // 메시지를 Redis로 직접 발행
    @MessageMapping("/chat/{roomId}/send")
    public void message(ChatMessageReqDto reqDto) {
        chatMessageService.publishMessageToRedis(reqDto);
    }
}
