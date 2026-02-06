package com.example.SeaTea;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
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

    return new OpenAPI()
        .addServersItem(new Server().url("/"))
        .components(components)
        .info(info)
        .addSecurityItem(securityRequirement);
  }
}
