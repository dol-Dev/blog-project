package com.doldev.dollog.domain.chat.application;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.doldev.dollog.domain.account.profile.entity.Profile;
import com.doldev.dollog.domain.account.profile.repository.ProfileRepository;
import com.doldev.dollog.domain.chat.dto.req.CreateChatRoomReqDto;
import com.doldev.dollog.domain.chat.dto.res.ChatRoomResDto;
import com.doldev.dollog.domain.chat.entity.ChatHistory;
import com.doldev.dollog.domain.chat.entity.ChatParticipant;
import com.doldev.dollog.domain.chat.entity.ChatRoom;
import com.doldev.dollog.domain.chat.repository.ChatHistoryRepository;
import com.doldev.dollog.domain.chat.repository.ChatParticipantRepository;
import com.doldev.dollog.domain.chat.repository.ChatRoomRepository;
import com.doldev.dollog.global.auth.principal.CustomUserDetails;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatHistoryRepository chatHistoryRepository;
    private final ProfileRepository profileRepository;

    // 채팅방 생성 또는 기존 채팅방 가져오기
    @Transactional
    public ChatRoomResDto getOrCreateChatRoom(CreateChatRoomReqDto req) {
        String owner = req.getOwner();
        String participant = req.getParticipant();
        boolean isSelfChat = owner.equals(participant);

        // 두 유저의 채팅방이 이미 존재하면 해당 방을 반환
        return chatRoomRepository.findByOwnerAndParticipant(owner, participant)
                .map(ChatRoomResDto::new)
                .orElseGet(() -> createChatRoom(owner, participant, isSelfChat));
    }

    // 새로운 채팅방 생성 및 초기 ChatHistory 기록 생성
    @Transactional
    private ChatRoomResDto createChatRoom(String owner, String participant, boolean isSelfChat) {
        ChatRoom chatRoom = new ChatRoom(owner, participant, isSelfChat);
        chatRoomRepository.save(chatRoom);

        // 채팅 참여자 추가
        chatParticipantRepository.save(new ChatParticipant(chatRoom, owner));
        if (!isSelfChat) {
            chatParticipantRepository.save(new ChatParticipant(chatRoom, participant));
        }

        // 채팅방 생성 시, 각 참여자에 대한 초기 입장 기록 생성
        chatHistoryRepository.save(new ChatHistory(chatRoom, owner));
        if (!isSelfChat) {
            chatHistoryRepository.save(new ChatHistory(chatRoom, participant));
        }

        return new ChatRoomResDto(chatRoom);
    }

    // 채팅방 나가기 (퇴장 기록 저장)
    @Transactional
    public void leaveChatRoom(int roomId, String participant) {
        ChatParticipant chatParticipant = chatParticipantRepository.findByChatRoomIdAndParticipant(roomId, participant)
                .orElseThrow(() -> new RuntimeException("채팅방 정보가 없습니다."));

        chatParticipant.leave(); // 상태를 LEFT로 갱신
        chatParticipantRepository.save(chatParticipant);

        // 사용자의 마지막 입장 기록 조회 후 퇴장 처리
        ChatHistory chatHistory = chatHistoryRepository
                .findTopByChatRoomIdAndParticipantOrderByEnteredAtDesc(roomId, participant)
                .orElseThrow(() -> new RuntimeException("채팅 기록이 없습니다."));
        chatHistory.leave(); // leftAt 기록
        chatHistoryRepository.save(chatHistory);
    }

    // 사용자가 참여 중인 채팅방 목록 조회
    public List<ChatRoomResDto> getMyChatRooms(CustomUserDetails userDetails) {
        return chatParticipantRepository.findByParticipant(userDetails.getNickname())
                .stream()
                .map(chatParticipant -> {
                    ChatRoom chatRoom = chatParticipant.getChatRoom();
                    Profile ownerProfile = profileRepository.findByNickname(chatRoom.getOwner()).orElse(null);
                    Profile participantProfile = profileRepository.findByNickname(chatRoom.getParticipant())
                            .orElse(null);
                    return new ChatRoomResDto(chatRoom, ownerProfile, participantProfile, chatParticipant.getStatus());
                })
                .collect(Collectors.toList());
    }

    // 채팅방 재입장 (참여자 추가 + 입장 기록 갱신 또는 생성)
    @Transactional
    public ChatRoomResDto reEnterChatRoom(int roomId, String participant) {
        // 기존 참여자 정보 조회
        ChatParticipant chatParticipant = chatParticipantRepository.findByChatRoomIdAndParticipant(roomId, participant)
                .orElseThrow(() -> new RuntimeException("채팅방 정보가 없습니다."));

        chatParticipant.reEnter(); // 상태를 ACTIVE로 갱신
        chatParticipantRepository.save(chatParticipant);

        // 기존 기록이 있으면 재입장 처리, 없으면 새 기록 생성
        ChatHistory chatHistory = chatHistoryRepository
                .findTopByChatRoomIdAndParticipantOrderByEnteredAtDesc(roomId, participant)
                .orElseGet(() -> {
                    ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                            .orElseThrow(() -> new RuntimeException("채팅방이 존재하지 않습니다."));
                    ChatHistory newHistory = new ChatHistory(chatRoom, participant);
                    return chatHistoryRepository.save(newHistory);
                });

        chatHistory.reEnter(); // enteredAt 갱신, leftAt 제거
        chatHistoryRepository.save(chatHistory);

        return new ChatRoomResDto(chatParticipant.getChatRoom());
    }
}
