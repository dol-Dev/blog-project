package com.doldev.dollog.domain.chat.dto.res;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.doldev.dollog.domain.account.profile.entity.Profile;
import com.doldev.dollog.domain.chat.entity.ChatMessage;
import com.doldev.dollog.global.auth.principal.CustomUserDetails;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ChatMessageResDto {
    private int messageId;
    private int roomId;

    private String sender;

    private String owner;
    private String ownerAvatar;

    private String participant;
    private String participantAvatar;

    private String provider;
    private String blogName;

    private String message;
    private String imageUrl;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdAt;

    public static ChatMessageResDto from(ChatMessage chatMessage, Profile ownerProfile,
            Profile participantProfile, CustomUserDetails userDetails) {
        ChatMessageResDto dto = new ChatMessageResDto();

        dto.messageId = chatMessage.getId();
        dto.roomId = chatMessage.getChatRoom().getId();

        dto.sender = chatMessage.getSender();

        dto.owner = ownerProfile.getNickname();
        dto.ownerAvatar = ownerProfile.getAvatarImageName();

        dto.participant = participantProfile.getNickname();
        dto.participantAvatar = participantProfile.getAvatarImageName();

        dto.message = chatMessage.getMessage();
        dto.imageUrl = chatMessage.getImageUrl();
        dto.createdAt = chatMessage.getCreatedAt();

        // 현재 로그인 정보를 이용하여 provider와 blogName 설정
        // provider과 blogName은 상대의 정보여야됨(본인의 정보x)
        if (userDetails.getNickname().equals(ownerProfile.getNickname())) {
            dto.provider = participantProfile.getSnsUser().getProvider().name();
            dto.blogName = participantProfile.getBlogName();
        } else if (userDetails.getNickname().equals(participantProfile.getNickname())) {
            dto.provider = ownerProfile.getSnsUser().getProvider().name();
            dto.blogName = ownerProfile.getBlogName();
        }
        return dto;
    }

    public static ChatMessageResDto from(ChatMessage chatMessage, Profile ownerProfile,
            Profile participantProfile, String sender) {
        ChatMessageResDto dto = new ChatMessageResDto();

        dto.messageId = chatMessage.getId();
        dto.roomId = chatMessage.getChatRoom().getId();

        dto.sender = sender;

        dto.owner = ownerProfile.getNickname();
        dto.ownerAvatar = ownerProfile.getAvatarImageName();

        dto.participant = participantProfile.getNickname();
        dto.participantAvatar = participantProfile.getAvatarImageName();

        dto.message = chatMessage.getMessage();
        dto.imageUrl = chatMessage.getImageUrl();
        dto.createdAt = chatMessage.getCreatedAt();

        // sender를 이용하여 provider와 blogName 설정
        if (sender.equals(ownerProfile.getNickname())) {
            dto.provider = participantProfile.getSnsUser().getProvider().name();
            dto.blogName = participantProfile.getBlogName();
        } else if (sender.equals(participantProfile.getNickname())) {
            dto.provider = ownerProfile.getSnsUser().getProvider().name();
            dto.blogName = ownerProfile.getBlogName();
        }
        return dto;
    }

}
