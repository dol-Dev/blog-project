package com.doldev.dollog.domain.chat.repository;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ChatRoomRedisRepository {

    // 유저가 입장한 채팅방과의 매핑 정보 저장
    private static final String ENTER_INFO = "ENTER_INFO"; // sessionId -> roomId

    @Resource(name = "redisTemplate")
    private final HashOperations<String, String, String> hashOpsEnterInfo;


    // 유저가 입장한 채팅방 ID와 유저 세션 ID를 Redis에 저장
    public void setUserEnterInfo(String sessionId, String roomId) {
        hashOpsEnterInfo.put(ENTER_INFO, sessionId, roomId);
    }

    // 세션 ID로 입장한 채팅방 ID 조회
    public String getUserEnterRoomId(String sessionId) {
        return hashOpsEnterInfo.get(ENTER_INFO, sessionId);
    }

    // 유저 세션 정보 삭제 (퇴장 시 호출)
    public void removeUserEnterInfo(String sessionId) {
        hashOpsEnterInfo.delete(ENTER_INFO, sessionId);
    }
}
