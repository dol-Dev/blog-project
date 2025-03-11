package com.doldev.dollog.domain.chat.dto.res;

import java.time.LocalDateTime;

import com.doldev.dollog.domain.chat.entity.ChatParticipant;

import lombok.Getter;

@Getter
public class ChatParticipantResDto {
    private String participant;
    private LocalDateTime joinedAt;

    public ChatParticipantResDto(ChatParticipant chatParticipant) {
        this.participant = chatParticipant.getParticipant();
        this.joinedAt = chatParticipant.getJoinedAt();
    }
}