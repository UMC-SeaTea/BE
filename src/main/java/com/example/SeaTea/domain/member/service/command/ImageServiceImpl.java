package com.example.SeaTea.domain.member.service.command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
      String extension = originalName.substring(originalName.lastIndexOf("."));
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
    // URL에서 파일명만 추출 (/api/images/uploads/uuid.png -> uuid.png)
    String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
    Path filePath = Paths.get(uploadDir).resolve(fileName).toAbsolutePath().normalize();

    try {
      Files.deleteIfExists(filePath);
    } catch (IOException e) {
      System.err.println("파일 삭제 실패: " + e.getMessage());
    }
  }
}
