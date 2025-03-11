package com.doldev.dollog.domain.chat.dto.req;

import lombok.Getter;

@Getter
public class CreateChatRoomReqDto {
    private String owner;
    private String participant;
}