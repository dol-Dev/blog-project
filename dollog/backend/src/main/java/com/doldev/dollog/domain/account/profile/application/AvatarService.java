package com.doldev.dollog.domain.account.profile.application;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;

@Service
public class AvatarService {

    @Value("${avatar.upload-dir}")
    private String uploadDir;

    @Value("${avatar.default-image}")
    private String defaultImage;

    private Path uploadPath;

    @PostConstruct
    public void init() throws IOException {
        // 업로드 디렉토리 설정
        uploadPath = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();
        Files.createDirectories(uploadPath);
    }

    // 사용자 정의 아바타를 저장하거나 기본 아바타를 반환
    public String saveAvatar(String nickname, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return createDefaultAvatar(nickname); // 기본 이미지 반환
        }

        String sanitizedName = sanitizeFilename(nickname);
        String extension = getFileExtension(file.getOriginalFilename());
        String uniqueFileName = generateUniqueFileName(sanitizedName, extension);

        Path targetPath = uploadPath.resolve(uniqueFileName);
        Files.createDirectories(targetPath.getParent());
        file.transferTo(targetPath);

        return uniqueFileName; // 상대 경로 대신 파일명만 반환
    }

    // 회원가입 시 기본 아바타 설정
    public String createDefaultAvatar(String nickname) throws IOException {
        String sanitizedName = sanitizeFilename(nickname);
        String extension = getFileExtension(defaultImage);
        String uniqueFileName = generateUniqueFileName(sanitizedName, extension);
    
        Path targetPath = uploadPath.resolve(uniqueFileName);
        
        // static에서 직접 복사
        try (InputStream is = new ClassPathResource("static/" + defaultImage).getInputStream()) {
            Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
        
        return uniqueFileName;
    }

    // 파일 삭제 로직
    public void deleteAvatar(String fileName) throws IOException {
        if (fileName == null || fileName.isBlank())
            return;

        Path deletePath = uploadPath.resolve(fileName).normalize();

        // 경로 무결성 검증
        if (!deletePath.startsWith(uploadPath)) {
            throw new SecurityException("Invalid file access: " + fileName);
        }

        Files.deleteIfExists(deletePath);
    }

    // 고유한 파일명 생성
    private String generateUniqueFileName(String baseName, String extension) {
        return baseName + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
    }

    // 불필요한 문자 제거
    private String sanitizeFilename(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]", "")
                .replaceAll("\\.\\./", "")
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-zA-Z0-9-_]", "");
    }

    // 확장자 추출
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return ".png"; // 확장자 없을 경우 기본값
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}