package com.doldev.dollog.domain.chat.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.doldev.dollog.domain.chat.application.ChatRoomService;
import com.doldev.dollog.domain.chat.dto.req.CreateChatRoomReqDto;
import com.doldev.dollog.domain.chat.dto.res.ChatRoomResDto;
import com.doldev.dollog.global.auth.principal.CustomUserDetails;
import com.doldev.dollog.global.dto.ApiResDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatRooms")
public class ChatRoomController {

        private final ChatRoomService chatRoomService;

        // 채팅방 생성 또는 기존 채팅방 가져오기
        @PostMapping("/between")
        public ResponseEntity<ApiResDto<?>> getOrCreateChatRoom(@RequestBody CreateChatRoomReqDto req) {
                ChatRoomResDto chatRoom = chatRoomService.getOrCreateChatRoom(req);
                return ResponseEntity.ok()
                                .body(ApiResDto.builder().messageCode("채팅방 생성 완료").data(chatRoom).build());
        }

        // 사용자가 속한 모든 채팅방 조회
        @GetMapping("/myRooms")
        public ResponseEntity<ApiResDto<?>> getMyChatRooms(@AuthenticationPrincipal CustomUserDetails userDetails) {
                List<ChatRoomResDto> rooms = chatRoomService.getMyChatRooms(userDetails);
                return ResponseEntity.ok()
                                .body(ApiResDto.builder().messageCode("내 채팅방 조회 완료").data(rooms).build());
        }

        // 채팅방 나가기
        @DeleteMapping("/{roomId}/leave")
        public ResponseEntity<ApiResDto<?>> leaveChatRoom(
                        @PathVariable int roomId, @RequestParam String participant) {
                chatRoomService.leaveChatRoom(roomId, participant);
                return ResponseEntity.ok()
                                .body(ApiResDto.builder().messageCode("채팅방 나가기 완료").build());
        }

        // 채팅방 재입장
        @PostMapping("/{roomId}/reenter")
        public ResponseEntity<ApiResDto<?>> reEnterChatRoom(
                        @PathVariable int roomId, @RequestParam String participant) {
                ChatRoomResDto chatRoom = chatRoomService.reEnterChatRoom(roomId, participant);
                return ResponseEntity.ok()
                                .body(ApiResDto.builder().messageCode("채팅방 재입장 완료").data(chatRoom).build());
        }
}
