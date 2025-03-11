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

    // User 일때
    public CustomUserDetails(User user) {
        this.user = user;
        this.snsUser = null;
        this.attributes = Map.of();
    }

    // SnsUser 일떄 
    public CustomUserDetails(SnsUser snsUser) {
        this.user = null;
        this.snsUser = snsUser;
        this.attributes = Map.of();
    }

    // SnsUser, User중 하나가 null이 아닐때
    public CustomUserDetails(User user, SnsUser snsUser) {
        if(user != null) {
            this.user = user;
        } else {
            this.snsUser = snsUser;
        }
        this.attributes = Map.of();
    }

    // SnsUser 회원가입 시
    public CustomUserDetails(SnsUser snsUser, Map<String, Object> attributes) {
        this.user = null;
        this.snsUser = snsUser;
        this.attributes = attributes != null ? attributes : Map.of();
    }

    public boolean isUser() {
        return user != null;
    }

    public boolean isSnsUser() {
        return snsUser != null;
    }

    public String getEmail() {
        return isUser() ? user.getEmail() : null;
    }

    public String getNickname() {
        return isUser() ? user.getProfile().getNickname() : snsUser.getProfile().getNickname();
    }

    public String getAvatarImageName() {
        return isUser() ? user.getProfile().getAvatarImageName() : snsUser.getProfile().getAvatarImageName();
    }

    public String getBlogName() {
        return isUser() ? user.getProfile().getBlogName() : snsUser.getProfile().getBlogName();
    }

    public String getProvider() {
        return isSnsUser() ? snsUser.getProvider().name() : null;
    }

    public int getId() {
        return isUser() ? user.getId() : snsUser.getId();
    }

    @Override
    public String getPassword() {
        return isUser() ? user.getPassword() : null;
    }

    @Override
    public String getUsername() {
        return isUser() ? user.getUsername() : snsUser.getUsername();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        RoleType role = isUser() ? user.getRole() : snsUser.getRole();
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    // OAuth2User을 위한 details
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return getUsername();
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
