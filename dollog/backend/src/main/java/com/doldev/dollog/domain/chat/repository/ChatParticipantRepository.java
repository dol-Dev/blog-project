package com.doldev.dollog.domain.chat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.doldev.dollog.domain.chat.entity.ChatParticipant;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
    List<ChatParticipant> findByChatRoomId(int chatRoomId);

    List<ChatParticipant> findByParticipant(String participant);

    Optional<ChatParticipant> findByChatRoomIdAndParticipant(int chatRoomId, String participant);

}
