package com.doldev.dollog.domain.chat.dto.req;

import lombok.Getter;

@Getter
public class ChatMessageReqDto {
    private int roomId; 
    private String message; 
    private String imageUrl; 
    private String sender;
}
