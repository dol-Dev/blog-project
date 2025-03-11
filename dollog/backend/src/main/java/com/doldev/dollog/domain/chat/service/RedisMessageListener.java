package com.doldev.dollog.domain.chat.service;

import java.nio.charset.StandardCharsets;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.doldev.dollog.domain.chat.dto.req.ChatMessageReqDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisMessageListener extends MessageListenerAdapter {
    private final SimpMessagingTemplate template;

    @SuppressWarnings("null")
    @Override
    public void onMessage(Message message, byte[] pattern) {
        ChatMessageReqDto chatmessage = convertMessage(message);
        template.convertAndSend("/topic/rooms/${roomId}" + chatmessage.getRoomId(), chatmessage);
    }

    private ChatMessageReqDto convertMessage(Message message) {
        String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);
        ObjectMapper objectMapper = new ObjectMapper();
        ChatMessageReqDto chatMessageReqDto = null;
        try {
            chatMessageReqDto = objectMapper.readValue(messageBody, ChatMessageReqDto.class);
        } catch (JsonProcessingException e) {
            log.error("메시지 변환 중 오류 발생", e);
        }
        return chatMessageReqDto;
    }
}
