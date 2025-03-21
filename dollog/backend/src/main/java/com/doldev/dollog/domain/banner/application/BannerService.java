package com.doldev.dollog.domain.banner.application;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.doldev.dollog.domain.account.profile.entity.Profile;
import com.doldev.dollog.domain.account.profile.repository.ProfileRepository;
import com.doldev.dollog.domain.account.snsUser.entity.SnsUser;
import com.doldev.dollog.domain.account.snsUser.repository.SnsUserRepository;
import com.doldev.dollog.domain.account.user.entity.User;
import com.doldev.dollog.domain.account.user.repository.UserRepository;
import com.doldev.dollog.domain.banner.dto.req.BannerReqDto;
import com.doldev.dollog.domain.banner.dto.res.LayoutRelatedInfoResDto;
import com.doldev.dollog.domain.banner.entity.Banner;
import com.doldev.dollog.global.auth.principal.CustomUserDetails;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BannerService {

    @PostConstruct
    public void init() throws IOException {
        // 업로드 디렉토리 설정
        uploadPath = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();
        Files.createDirectories(uploadPath);
    }

    private final UserRepository userRepository;
    private final SnsUserRepository snsUserRepository;
    private final ProfileRepository profileRepository;

    @Value("${banner.upload-dir}")
    private String uploadDir;
    private Path uploadPath;

    public void createBannerDescription(CustomUserDetails userDetails, BannerReqDto reqDto) {
        Banner banner;
        if(userDetails.getBanner() != null) {
            banner = userDetails.getBanner();
        } else {
            banner = new Banner();
        }
        
        // 배너 설명 저장
        banner.changeBannerDescription(reqDto.getBannerDescription());
        
        // 사용자 유형에 따라 저장
        if (userDetails.getUser() != null) {
            User user = userDetails.getUser();
            user.assignBanner(banner);
            userRepository.save(user);
        } else if (userDetails.isSnsUser()) {
            SnsUser snsUser = userDetails.getSnsUser();
            snsUser.assignBanner(banner);
            snsUserRepository.save(snsUser);
        }
    }
    

    public void createBannerImage(CustomUserDetails userDetails, MultipartFile bannerFile) throws IOException {
        Banner banner;
        if(userDetails.getBanner() != null) {
            banner = userDetails.getBanner();
        } else {
            banner = new Banner();
        }

        // 프로필 조회
        Profile profile = profileRepository.findByNickname(userDetails.getNickname())
                .orElseThrow(() -> new IllegalArgumentException("Profile not found for nickname: " + userDetails.getNickname()));
    
        // 배너 이미지 저장
        banner.changeBannerImageName(uploadBannerImage(profile.getNickname(), bannerFile));
    
        // 사용자 유형에 따라 저장
        if (userDetails.getUser() != null) {
            User user = userDetails.getUser();
            user.assignBanner(banner);
            userRepository.save(user);
        } else if (userDetails.isSnsUser()) {
            SnsUser snsUser = userDetails.getSnsUser();
            snsUser.assignBanner(banner);
            snsUserRepository.save(snsUser);
        }
    }
    

    // 사용자 정의 배너 저장
    public String uploadBannerImage(String nickname, MultipartFile file) throws IOException {
        String sanitizedName = sanitizeFilename(nickname);
        String extension = getFileExtension(file.getOriginalFilename());
        String uniqueFileName = generateUniqueFileName(sanitizedName, extension);

        Path targetPath = uploadPath.resolve(uniqueFileName);
        Files.createDirectories(targetPath.getParent());

        // file.transferTo 대신 파일의 InputStream을 복사하는 방식 사용
        try (InputStream is = file.getInputStream()) {
            Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
        return uniqueFileName;
    }

    // 파일 삭제 로직
    public void deleteBanner(String fileName) throws IOException {
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

    public LayoutRelatedInfoResDto findLayoutInfoByPrincipal(CustomUserDetails userDetails) {
        if (userDetails.isUser()) {
            return userRepository.findById(userDetails.getId())
                    .map(LayoutRelatedInfoResDto::fromEntity)
                    .orElse(null);
        } else if (userDetails.isSnsUser()) {
            return snsUserRepository.findById(userDetails.getId())
                    .map(LayoutRelatedInfoResDto::fromEntity)
                    .orElse(null);
        }
        return null;
    }

    public LayoutRelatedInfoResDto findLayoutInfoByNickname(String nickname) {
        Optional<Profile> profile = profileRepository.findByNickname(nickname);
        if (profile.isPresent()) {
            if (profile.get().getUser() != null) {
                return LayoutRelatedInfoResDto.fromEntity(profile.get().getUser());
            } else if (profile.get().getSnsUser() != null) {
                return LayoutRelatedInfoResDto.fromEntity(profile.get().getSnsUser());
            }
        }
        return null;
    }
}