package com.doldev.dollog.domain.chat.dto.res;

import java.time.LocalDateTime;

import com.doldev.dollog.domain.account.profile.entity.Profile;
import com.doldev.dollog.domain.chat.entity.ChatParticipantStatus;
import com.doldev.dollog.domain.chat.entity.ChatRoom;

import lombok.Getter;

@Getter
public class ChatRoomResDto {
    private int roomId;
    private String owner;
    private String ownerAvatar;
    private String participant;
    private String participantAvatar;
    private boolean isSelfChat;
    private LocalDateTime createdAt;
    private ChatParticipantStatus participantStatus;

    public ChatRoomResDto(ChatRoom chatRoom, Profile ownerProfile, Profile participantProfile, ChatParticipantStatus participantStatus) {
        this.roomId = chatRoom.getId();
        this.owner = chatRoom.getOwner();
        this.ownerAvatar = ownerProfile != null ? ownerProfile.getAvatarImageName() : null;
        this.participant = chatRoom.getParticipant();
        this.participantAvatar = participantProfile != null ? participantProfile.getAvatarImageName() : null;
        this.isSelfChat = chatRoom.isSelfChat();
        this.createdAt = chatRoom.getCreatedAt();
        this.participantStatus = participantStatus;
    }

    public ChatRoomResDto(ChatRoom chatRoom) {
        this.roomId = chatRoom.getId();
        this.owner = chatRoom.getOwner();
        this.participant = chatRoom.getParticipant();
        this.isSelfChat = chatRoom.isSelfChat();
        this.createdAt = chatRoom.getCreatedAt();
    }
}