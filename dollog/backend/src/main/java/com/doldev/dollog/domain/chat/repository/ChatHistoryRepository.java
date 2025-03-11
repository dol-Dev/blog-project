package com.doldev.dollog.domain.chat.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.doldev.dollog.domain.chat.entity.ChatHistory;

@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Integer> {
    Optional<ChatHistory> findTopByChatRoomIdAndParticipantOrderByEnteredAtDesc(
            int chatRoomId, String participant);
}