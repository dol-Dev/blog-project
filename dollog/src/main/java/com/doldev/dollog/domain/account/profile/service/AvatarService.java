package com.doldev.dollog.domain.account.profile.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;

@Service
public class AvatarService {

    @Value("${avatar.upload-dir}")
    private String uploadDir;

    @Value("${avatar.default-image}")
    private String defaultImage;

    // 아바타 저장 (업로드 파일 있을 때)
    public String saveAvatar(String nickname, MultipartFile file) throws IOException {
        Path targetPath = getAvatarPath(nickname);
        Files.createDirectories(targetPath.getParent());
        
        if(file != null && !file.isEmpty()) {
            file.transferTo(targetPath);
        }
        return targetPath.toString();
    }

    // 기본 아바타 생성 (업로드 파일 없을 때)
    public String createDefaultAvatar(String nickname) throws IOException {
        Path targetPath = getAvatarPath(nickname);
        Path defaultPath = Paths.get(uploadDir, defaultImage);

        if(!Files.exists(defaultPath)) {
            throw new FileNotFoundException("기본 아바타 이미지가 없습니다: " + defaultPath);
        }

        Files.copy(defaultPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        return targetPath.toString();
    }

    // 아바타 삭제
    public void deleteAvatar(String path) throws IOException {
        if(path != null && !path.isBlank()) {
            Files.deleteIfExists(Paths.get(path));
        }
    }

    private Path getAvatarPath(String nickname) {
        return Paths.get(uploadDir, nickname + ".jpg");
    }
}