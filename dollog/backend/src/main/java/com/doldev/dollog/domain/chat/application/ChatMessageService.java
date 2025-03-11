package com.doldev.dollog.domain.chat.application;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.doldev.dollog.domain.account.profile.entity.Profile;
import com.doldev.dollog.domain.account.profile.repository.ProfileRepository;
import com.doldev.dollog.domain.chat.dto.req.ChatMessageReqDto;
import com.doldev.dollog.domain.chat.dto.res.ChatMessageResDto;
import com.doldev.dollog.domain.chat.entity.ChatHistory;
import com.doldev.dollog.domain.chat.entity.ChatMessage;
import com.doldev.dollog.domain.chat.entity.ChatRoom;
import com.doldev.dollog.domain.chat.repository.ChatHistoryRepository;
import com.doldev.dollog.domain.chat.repository.ChatMessageRepository;
import com.doldev.dollog.domain.chat.repository.ChatRoomRepository;
import com.doldev.dollog.global.auth.principal.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatMessageService {
        private final SimpMessagingTemplate template;
        private final ObjectMapper objectMapper;
        private final ChatMessageRepository chatMessageRepository;
        private final ChatRoomRepository chatRoomRepository;
        private final ChatHistoryRepository chatHistoryRepository;
        private final ProfileRepository profileRepository;

        // 특정 사용자의 채팅방 메시지 조회 (입장 시간 이후 메시지만)
        @Transactional
        public List<ChatMessageResDto> getChatRoomChatMessages(int roomId, CustomUserDetails userDetails) {
                ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                                .orElseThrow(() -> new NullPointerException("존재하지 않는 채팅방입니다."));

                // owner, participant의 프로필 조회
                Profile ownerProfile = profileRepository.findByNickname(chatRoom.getOwner())
                                .orElseThrow(() -> new RuntimeException("존재하지 않는 프로필 (owner)"));
                Profile participantProfile = profileRepository.findByNickname(chatRoom.getParticipant())
                                .orElseThrow(() -> new RuntimeException("존재하지 않는 프로필 (participant)"));

                // 사용자의 마지막 입장 기록 조회
                Optional<ChatHistory> chatHistoryOpt = chatHistoryRepository
                                .findTopByChatRoomIdAndParticipantOrderByEnteredAtDesc(
                                                roomId, userDetails.getNickname());

                // 채팅 기록이 없으면 빈 리스트 반환
                if (chatHistoryOpt.isEmpty()) {
                        return Collections.emptyList();
                }

                // 사용자의 마지막 입장 시간 이후의 메시지만 조회
                LocalDateTime enteredAt = chatHistoryOpt.get().getEnteredAt();
                List<ChatMessage> chatMessages = chatMessageRepository.findMessagesAfterEntry(chatRoom, enteredAt);

                return chatMessages.stream()
                                .map(cm -> ChatMessageResDto.from(cm, ownerProfile, participantProfile, userDetails))
                                .toList();
        }

        // 채팅방에 메시지 발송
        public void publishMessageToRedis(ChatMessageReqDto reqDto) {
                // 채팅방 존재 여부 확인
                ChatRoom chatRoom = chatRoomRepository.findById(reqDto.getRoomId())
                                .orElseThrow(() -> new RuntimeException("존재하지 않는 채팅방"));

                // sender에 해당하는 프로필은 필요하지만, 여기서는 채팅방의 owner, participant 둘 다 필요하므로:
                Profile ownerProfile = profileRepository.findByNickname(chatRoom.getOwner())
                                .orElseThrow(() -> new RuntimeException("존재하지 않는 프로필 (owner)"));
                Profile participantProfile = profileRepository.findByNickname(chatRoom.getParticipant())
                                .orElseThrow(() -> new RuntimeException("존재하지 않는 프로필 (participant)"));

                // 채팅 메시지 저장
                ChatMessage cm = ChatMessage.builder()
                                .chatRoom(chatRoom)
                                .sender(reqDto.getSender())
                                .message(reqDto.getMessage())
                                .imageUrl(reqDto.getImageUrl())
                                .createdAt(LocalDateTime.now())
                                .build();
                chatMessageRepository.save(cm);

                // 오버로드된 정적 팩토리 메서드를 사용하여 DTO 생성 후 WebSocket으로 전송
                ChatMessageResDto dto = ChatMessageResDto.from(cm, ownerProfile, participantProfile,
                                reqDto.getSender());
                sendMessageToWebSocket(dto);
        }

        // WebSocket으로 메시지 전송
        private void sendMessageToWebSocket(ChatMessageResDto chatMessageResDto) {
                try {
                        String messageJson = objectMapper.writeValueAsString(chatMessageResDto);
                        template.convertAndSend("/topic/rooms/" + chatMessageResDto.getRoomId(), messageJson);
                } catch (Exception e) {
                        log.error("Failed to publish message to Redis", e);
                }
        }
}
