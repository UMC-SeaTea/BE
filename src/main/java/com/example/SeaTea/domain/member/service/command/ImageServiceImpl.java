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
    // 폴더 생성
    Path rootPath = Paths.get(uploadDir).toAbsolutePath().normalize();
    try {
      if (!Files.exists(rootPath)) {
        Files.createDirectories(rootPath);
      }
      // 파일명 중복 방지(UUID 사용)
      String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
      Path filePath = Paths.get(uploadDir).resolve(fileName);

      // 파일 복사
      Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

      // 접근 가능 URL 반환
      return "/api/images/uploads/" + fileName; // 접근 가능한 상대 경로 또는 전체 URL 반환
    } catch (IOException e) {
      throw new RuntimeException("파일 저장에 실패했습니다.", e);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public void delete(String imageUrl) {
  }
}
