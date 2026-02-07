package com.example.SeaTea.global.config;

import com.example.SeaTea.global.auth.Kakao.KakaoOAuth2UserService;
import com.example.SeaTea.global.config.handler.CustomFailureHandler;
import com.example.SeaTea.global.config.handler.CustomLogoutSuccessHandler;
import com.example.SeaTea.global.config.handler.CustomSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

  private final CustomSuccessHandler customSuccessHandler;
  private final CustomFailureHandler customFailureHandler;
  private final CustomLogoutSuccessHandler customLogoutSuccessHandler;
  private final KakaoOAuth2UserService kakaoOAuth2UserService;

  private final String[] allowUris = {
      "/api/login",
      "/api/sign-up",
      "/swagger-ui/**",
      "/swagger-resources/**",
      "/v3/api-docs/**",
      "/error",
      "/api/**",

      // 소셜 로그인
      "/oauth2/**",
      "/api/callback",

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
        // 로그인 시작 주소: /oauth2/authorization/kakao
        // 콜백 주소: /login/oauth2/code/kakao
        .oauth2Login(oauth2 -> oauth2
            .loginProcessingUrl("/api/callback")
            .loginPage("/api/login")
            // success/failure Handler로 더 상세하게
            .defaultSuccessUrl("/api/users/me", true)
            .failureUrl("/api/users /me")
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
        );

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }


  // ********* 관리자 페이지 테스트
//  private final String[] allowUris = {
//    "/sign-up",
//           //"/swagger-ui/**",
//           //"/swagger-resources/**",
//           //"/v3/api-docs/**",
//  };
//
//  @Bean
//  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//    http
//        .authorizeHttpRequests(requests -> requests
//            .requestMatchers(allowUris).permitAll()
//            .requestMatchers("/swagger-ui/index.html").hasRole("ADMIN")
//            .anyRequest().authenticated()
//        )
//        .formLogin(form -> form
//            .defaultSuccessUrl("/", true)
//            .permitAll()
//        )
//        .csrf(AbstractHttpConfigurer::disable)
//        .logout(logout -> logout
//            .logoutUrl("/logout")
//            .logoutSuccessUrl("/login?logout")
//            .permitAll()
//        );
//
//    return http.build();
//  }
//
//  @Bean
//  public PasswordEncoder passwordEncoder() {
//    return new BCryptPasswordEncoder();
//  }
}