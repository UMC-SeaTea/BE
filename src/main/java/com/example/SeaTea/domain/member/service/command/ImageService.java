package com.example.SeaTea.domain.member.service.command;

import org.springframework.web.multipart.MultipartFile;

public interface ImageService {

  String upload(MultipartFile file);

  // 수정 시 기존 이미지 삭제를 위함
  void delete(String imageUrl);
}
