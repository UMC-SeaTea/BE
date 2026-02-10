package com.example.SeaTea.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Value("${file.upload-dir}")
  private String uploadDir;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // 브라우저에서 접근할 URL 패턴 정의
    registry.addResourceHandler("/api/images/uploads/**")
        // 실제 파일이 있는 물리적 경로 매핑 (반드시 file: 접두어와 마지막 /가 필요함)
        .addResourceLocations("file:/path/to/your/upload/dir/");
  }

}
