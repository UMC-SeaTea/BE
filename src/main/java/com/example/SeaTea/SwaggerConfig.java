package com.example.SeaTea;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI myInfraAPI() {
    Info info = new Info()
        .title("SeaTea")
        .version("1.0")
        .description("SeaTea API 문서");

    // JWT 토큰 헤더 방식
    String securityScheme = "JWT TOKEN";
    SecurityRequirement securityRequirement = new SecurityRequirement().addList(securityScheme);

    Components components = new Components()
        .addSecuritySchemes(securityScheme, new SecurityScheme()
            .name(securityScheme)
            .type(SecurityScheme.Type.HTTP)
            .scheme("Bearer")
            .bearerFormat("JWT"));


    // 코드 로직과 관련X controller에 login api 추가 *****
    // 로그인 데이터 구조(ID/PW)
    // 개별 필드 스키마 생성
    Schema<String> usernameSchema = new Schema<String>();
    usernameSchema.setType("string");
    usernameSchema.setDescription("사용자 아이디");

    Schema<String> passwordSchema = new Schema<String>();
    passwordSchema.setType("string");
    passwordSchema.setFormat("password");
    passwordSchema.setDescription("비밀번호");

    // 메인 로그인 스키마 생성 및 필드 추가
    Schema<Object> loginSchema = new Schema<Object>();
    loginSchema.setType("object");
    loginSchema.addProperty("username", usernameSchema);
    loginSchema.addProperty("password", passwordSchema);

    // MediaType 및 Content 조립
    MediaType mediaType = new MediaType();
    mediaType.schema(loginSchema);

    Content content = new Content();
    content.addMediaType("application/x-www-form-urlencoded", mediaType);

    // Operation 조립
    Operation loginOperation = new Operation()
        .summary("세션 로그인")
        .description("Spring Security 기본 폼 로그인을 이용한 인증")
        .tags(Collections.singletonList("Auth"))  // Auth 그룹으로 묶기
        .requestBody(new RequestBody().content(content))
        .responses(new ApiResponses()
            .addApiResponse("200", new ApiResponse().description("로그인 성공"))
            .addApiResponse("401", new ApiResponse().description("인증 실패")));

    return new OpenAPI()
        .addServersItem(new Server().url("/"))
        .info(info)
        .components(components) // 위에서 정의한 JWT 설정 포함
        .addSecurityItem(securityRequirement) // 전역 보안 요구사항 적용
        .path("/login", new PathItem().post(loginOperation)); // 로그인 경로 강제 추가******
  }
}