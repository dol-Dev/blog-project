package com.doldev.dollog.domain.account.profile.dto.req;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;

@Getter
public class AvatarUpdateReqDto {
    private MultipartFile avatarFile;
}