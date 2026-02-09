package com.example.SeaTea.global.config;

import com.example.SeaTea.global.auth.Kakao.KakaoOAuth2UserService;
import com.example.SeaTea.global.config.handler.CustomFailureHandler;
import com.example.SeaTea.global.config.handler.CustomLogoutSuccessHandler;
import com.example.SeaTea.global.config.handler.CustomSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

  private final CustomSuccessHandler customSuccessHandler;
  private final CustomFailureHandler customFailureHandler;
  private final CustomLogoutSuccessHandler customLogoutSuccessHandler;
  private final KakaoOAuth2UserService kakaoOAuth2UserService;
  private final CorsConfigurationSource corsConfigurationSource;

  private final String[] allowUris = {
      "/api/login",
      "/api/sign-up",
      "/swagger-ui/**",
      "/swagger-resources/**",
      "/v3/api-docs/**",
      "/error",
      "/api/users/me",

      // api 연동 확인할 때만 선택적으로 주석 풀어서 하기(주석 풀면 인증 없이 /api/** 모든 접근 가능)
//      "/api/**",

      // 소셜 로그인
      "/oauth2/**",
      "/api/callback",

      //Diagnosis 테스트용 (로그인 전)
      "/api/diagnosis/test/**",

      // 콘솔 로그에 favicon 제거
      "/favicon.ico"
  };

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(requests -> requests
            .requestMatchers(allowUris).permitAll()
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated()
        )

        .formLogin(form -> form
                .loginProcessingUrl("/api/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(customSuccessHandler)
                .failureHandler(customFailureHandler)
                .permitAll()
//                .defaultSuccessUrl("/swagger-ui/index.html", true)
        )

        // 소셜 로그인 설정
        // 로그인 시작 주소: http://localhost:8080/oauth2/authorization/kakao
        // 콜백 주소: /login/oauth2/code/kakao
        .oauth2Login(oauth2 -> oauth2
            .loginProcessingUrl("/api/callback")

            // 미인증 접근 시 Security가 401 Unauthorized 대신 설정된 /api/login로 로드
//            .loginPage("/api/login")

            // success/failure Handler로 더 상세하게
            .defaultSuccessUrl("/api/users/me", true)
            .failureUrl("/api/users/me")
            .userInfoEndpoint(userInfo -> userInfo
                .userService(kakaoOAuth2UserService)
            )
        )

//        csrf 비활성화
        .csrf(AbstractHttpConfigurer::disable)
        .logout(logout -> logout
                .logoutUrl("/api/logout")
                .logoutSuccessHandler(customLogoutSuccessHandler)
                .permitAll()
//            .logoutSuccessUrl("/login?logout")
        )

        // SecurityConfig.java의 filterChain 내부에 추가
        .exceptionHandling(conf -> conf
          .authenticationEntryPoint((request, response, authException) -> {
          // 페이지 이동(302) 대신 401 에러 코드 전송
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
          })
        );


    // CORS 설정
    http.cors(cors -> cors.configurationSource(corsConfigurationSource));

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

}