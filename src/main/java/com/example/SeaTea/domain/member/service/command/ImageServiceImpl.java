package com.example.SeaTea.domain.member.service.command;

import com.example.SeaTea.domain.member.exception.MemberException;
import com.example.SeaTea.domain.member.exception.code.MemberErrorCode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

// 로컬 저장용 구현체
@Service
@Primary // 기본적으로 이 bean 사용하도록 설정
public class ImageServiceImpl implements ImageService {

  @Value("${file.upload-dir}")
  private String uploadDir; // yml에 설정한 로컬 경로

  @Override
  public String upload(MultipartFile file) {
    try {
      // 폴더 생성
      Path rootPath = Paths.get(uploadDir).toAbsolutePath().normalize();
      if (!Files.exists(rootPath)) {
        Files.createDirectories(rootPath);
      }
      // 확장자 추출 및 안전한 파일명 생성
      String originalName = file.getOriginalFilename();

      List<String> allowedExtensions = List.of(".jpg", ".jpeg", ".png", ".gif", ".webp");
      List<String> allowedContentTypes = List.of("image/jpeg", "image/png", "image/gif", "image/webp");

      if (originalName == null || !originalName.contains(".")) {
        throw new MemberException(MemberErrorCode._INVALID_FILENAME);
      }
      String extension = originalName.substring(originalName.lastIndexOf("."));
      if (!allowedExtensions.contains(extension.toLowerCase())) {
        throw new MemberException(MemberErrorCode._UNALLOWED_FILENAME);
      }
      String contentType = file.getContentType();
      if (contentType == null || !allowedContentTypes.contains(contentType.toLowerCase())) {
        throw new MemberException(MemberErrorCode._UNALLOWED_FILENAME);
      }
      String fileName = UUID.randomUUID().toString() + extension;

      // 파일 저장
      Path targetLocation = rootPath.resolve(fileName);
      Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

      // 접근 가능 URL 반환
      return "/api/images/uploads/" + fileName; // 접근 가능한 상대 경로 또는 전체 URL 반환
    } catch (IOException e) {
      throw new RuntimeException("파일 저장에 실패했습니다.", e);
    }
  }

  @Override
  public void delete(String imageUrl) {
    if (imageUrl == null || imageUrl.isBlank()) return;

    // URL에서 파일명만 추출 (/api/images/uploads/uuid.png -> uuid.png)
    String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
    Path rootPath = Paths.get(uploadDir).toAbsolutePath().normalize();
    Path filePath = rootPath.resolve(fileName).toAbsolutePath().normalize();
        // uploadDir 외부 접근 방지
     if (!filePath.startsWith(rootPath)) {
       throw new RuntimeException("잘못된 파일 경로입니다.");
     }

    try {
      Files.deleteIfExists(filePath);
    } catch (IOException e) {
      System.err.println("파일 삭제 실패: " + e.getMessage());
    }
  }
}
