package com.doldev.dollog.global.auth.principal;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.doldev.dollog.domain.account.roletype.enums.RoleType;
import com.doldev.dollog.domain.account.snsUser.entity.SnsUser;
import com.doldev.dollog.domain.account.user.entity.User;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomUserDetails implements UserDetails, OAuth2User {

    private User user;
    private SnsUser snsUser;
    private Map<String, Object> attributes;

    // 일반 회원 생성자
    public CustomUserDetails(User user) {
        this.user = user;
        this.snsUser = null;
        this.attributes = Map.of();
    }

    // SNS(카카오, 네이버) 로그인 회원 생성자
    public CustomUserDetails(SnsUser snsUser) {
        this.snsUser = snsUser;
        this.user = null;
        this.attributes = Map.of();
    }

    // 구글 로그인 회원 생성자
    public CustomUserDetails(SnsUser snsUser, Map<String, Object> attributes) {
        this.snsUser = snsUser;
        this.user = null;
        this.attributes = attributes != null ? attributes : Map.of();
    }


    // OAuth2User 구현
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return getUsername();
    }

    // UserDetails 구현
    @Override
    public String getPassword() {
        return user != null ? user.getPassword() : "";
    }

    @Override
    public String getUsername() {
        return user != null ? user.getUsername() : snsUser.getSnsIdentifier();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        RoleType role = user != null ? user.getRole() : snsUser.getRole();
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    // 계정 상태 관련 메서드
    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    
    // 유틸리티 메서드
    public boolean isRegularUser() {
        return user != null;
    }

    public boolean isSnsUser() {
        return snsUser != null;
    }

    public String getEmail() {
        return user != null ? user.getEmail() : "";
    }

    // 속성 설정 메서드 (OAuth2 전용)
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
}