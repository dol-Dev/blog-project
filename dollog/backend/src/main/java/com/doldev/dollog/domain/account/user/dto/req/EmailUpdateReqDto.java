package com.doldev.dollog.domain.account.user.dto.req;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailUpdateReqDto {
    private String username;
    private String newEmail;

}