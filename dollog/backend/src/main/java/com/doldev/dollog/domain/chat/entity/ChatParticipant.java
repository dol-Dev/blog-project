package com.doldev.dollog.domain.chat.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "chat_participant")
public class ChatParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "chatroom_id", nullable = false)
    private ChatRoom chatRoom;

    @Column(nullable = false)
    private String participant; // 참여자의 닉네임

    @Enumerated(EnumType.STRING)
    private ChatParticipantStatus status; // ACTIVE or LEFT

    @Column(nullable = false)
    private LocalDateTime joinedAt = LocalDateTime.now(); // 참여한 시간

    public void leave() {
        this.status = ChatParticipantStatus.LEFT;
    }

    public void reEnter() {
        this.status = ChatParticipantStatus.ACTIVE;
    }

    public ChatParticipant(ChatRoom chatRoom, String participant) {
        this.chatRoom = chatRoom;
        this.participant = participant;
        this.status = ChatParticipantStatus.ACTIVE;
        this.joinedAt = LocalDateTime.now();
    }
}