package com.doldev.dollog.domain.chat.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "chat_room", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "owner", "participant" })
}) // 같은 사람끼리 중복된 방 생성 방지
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String owner; // 블로그 주인의 닉네임

    @Column(nullable = false)
    private String participant; // 방문자의 닉네임 (또는 자기 자신)

    @Column(nullable = false)
    private boolean isSelfChat; // 자기 자신과 채팅 여부

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages = new ArrayList<>();

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatParticipant> participants = new ArrayList<>();

    public ChatRoom(String owner, String participant, boolean isSelfChat) {
        this.owner = owner;
        this.participant = participant;
        this.isSelfChat = isSelfChat;
    }
}
