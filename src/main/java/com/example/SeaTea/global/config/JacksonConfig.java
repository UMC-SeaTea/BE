package com.example.SeaTea.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.jackson2.CoreJackson2Module;
import org.springframework.security.oauth2.client.jackson2.OAuth2ClientJackson2Module;
import org.springframework.security.web.jackson2.WebJackson2Module;

@Configuration
public class JacksonConfig {

  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    // 보안 취약점 방지를 위한 최소한의 모듈만 등록
    mapper.registerModules(
        new CoreJackson2Module(),
        new WebJackson2Module(),
        new OAuth2ClientJackson2Module()
    );
    return mapper;
  }
}
