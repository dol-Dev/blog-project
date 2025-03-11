package com.doldev.dollog.domain.chat.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.doldev.dollog.domain.chat.entity.ChatMessage;
import com.doldev.dollog.domain.chat.entity.ChatRoom;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {

        // 특정 사용자의 마지막 입장 시간 이후 메시지만 조회
        @Query("SELECT cm FROM ChatMessage cm " +
                        "WHERE cm.chatRoom = :chatRoom " +
                        "AND cm.createdAt > :enteredAt")
        List<ChatMessage> findMessagesAfterEntry(
                        @Param("chatRoom") ChatRoom chatRoom,
                        @Param("enteredAt") LocalDateTime enteredAt);
}