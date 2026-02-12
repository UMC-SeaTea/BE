package com.example.SeaTea.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.jackson2.CoreJackson2Module;
import org.springframework.security.oauth2.client.jackson2.OAuth2ClientJackson2Module;
import org.springframework.security.web.jackson2.WebJackson2Module;

@Configuration
public class JacksonConfig {

  // 일반적인 API 처리
  @Bean
  @Primary // 스프링이 ObjectMapper를 달라고 할 때 우선순위를 가짐(토큰 없을 때)
  public ObjectMapper objectMapper() {
    return new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  // Redis나 세션 저장소에 보안 객체를 넣을 때 사용
  @Bean(name = "securityObjectMapper")
  public ObjectMapper securityObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());

    // 보안 모듈 등록 (타입 정보 포함됨)
    mapper.registerModules(
        new CoreJackson2Module(),
        new WebJackson2Module(),
        new OAuth2ClientJackson2Module()
    );
    return mapper;
  }
}
