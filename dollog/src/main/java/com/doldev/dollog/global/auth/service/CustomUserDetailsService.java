package com.doldev.dollog.global.auth.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.doldev.dollog.domain.account.snsUser.enums.SnsProvider;
import com.doldev.dollog.domain.account.snsUser.repository.SnsUserRepository;
import com.doldev.dollog.domain.account.user.repository.UserRepository;
import com.doldev.dollog.global.auth.principal.CustomUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final SnsUserRepository snsUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("일반 회원 조회: {}", username);

        return userRepository.findByUsername(username)
                .map(user -> {
                    log.info("일반 회원 로그인 성공: {}", username);
                    return new CustomUserDetails(user);
                })
                .orElseThrow(() -> {
                    log.warn("일반 회원 없음: {}", username);
                    return new UsernameNotFoundException("일반 회원을 찾을 수 없습니다: " + username);
                });
    }

    public UserDetails loadSnsUserBySnsIdentifier(String snsIdentifier, SnsProvider provider) {
        log.debug("SNS 회원 조회: provider={}, snsId={}", provider, snsIdentifier);

        return snsUserRepository.findBySnsIdentifierAndProvider(snsIdentifier, provider)
                .map(snsUser -> {
                    log.info("SNS 회원 로그인 성공: {} - {}", provider, snsIdentifier);
                    return new CustomUserDetails(snsUser);
                })
                .orElseThrow(() -> {
                    log.warn("SNS 회원 없음: {} - {}", provider, snsIdentifier);
                    return new UsernameNotFoundException(
                            String.format("%s 계정을 찾을 수 없습니다: %s", provider, snsIdentifier));
                });
    }
}