package com.doldev.dollog.domain.chat.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "chat_history")
public class ChatHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "chatroom_id", nullable = false)
    private ChatRoom chatRoom;

    @Column(nullable = false)
    private String participant;  // 입장한 유저의 닉네임

    @Column(nullable = false)
    private LocalDateTime enteredAt; // 마지막 입장 시간

    @Column
    private LocalDateTime leftAt; // 마지막 퇴장 시간

    public ChatHistory(ChatRoom chatRoom, String participant) {
        this.chatRoom = chatRoom;
        this.participant = participant;
        this.enteredAt = LocalDateTime.now();
    }

    public void leave() {
        this.leftAt = LocalDateTime.now();
    }

    public void reEnter() {
        this.enteredAt = LocalDateTime.now();
        this.leftAt = null;
    }
}
