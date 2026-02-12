package com.example.SeaTea.global.config;

import com.example.SeaTea.global.auth.entity.JsonLoginFilter;
import com.example.SeaTea.global.auth.Kakao.KakaoOAuth2UserService;
import com.example.SeaTea.global.auth.entity.JwtAuthenticationFilter;
import com.example.SeaTea.global.auth.entity.JwtTokenProvider;
import com.example.SeaTea.global.auth.repository.HttpCookieOAuth2AuthorizationRequestRepository;
import com.example.SeaTea.global.config.handler.CustomFailureHandler;
import com.example.SeaTea.global.config.handler.CustomLogoutSuccessHandler;
import com.example.SeaTea.global.config.handler.CustomSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
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
  private final AuthenticationConfiguration authenticationConfiguration;
  private final JwtTokenProvider jwtTokenProvider;
  private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

  private final String[] allowUris = {
      "/api/login",
      "/api/sign-up",
      "/swagger-ui/**",
      "/swagger-resources/**",
      "/v3/api-docs/**",
      "/error",
      "/api/users/me",
      "/api/images/uploads/**",

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
  public AuthenticationManager authenticationManager() throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  @Bean
  public JsonLoginFilter jsonLoginFilter() throws Exception {
    JsonLoginFilter filter = new JsonLoginFilter();
    // 필터에 AuthenticationManager 설정
    filter.setAuthenticationManager(authenticationManager());

    // 핸들러 커스텀 필터에 연결
    filter.setAuthenticationSuccessHandler(customSuccessHandler);
    filter.setAuthenticationFailureHandler(customFailureHandler);

    // 만약 로그인이 수행될 URL을 필터 생성자 외에 여기서도 지정
//     filter.setFilterProcessesUrl("/api/login");
     return filter;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        // 세션 끄기
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .authorizeHttpRequests(requests -> requests
            .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/spaces/**").permitAll()
            .requestMatchers(allowUris).permitAll()
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated()
        )

        // 소셜 로그인 설정
        // 로그인 시작 주소: http://localhost:8080/oauth2/authorization/kakao
        // 콜백 주소: /login/oauth2/code/kakao
        .oauth2Login(oauth2 -> oauth2
            // authorizationEndpoint로 저장소 설정
            .authorizationEndpoint(authorization -> authorization
                .authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository)
            )
            // userInfoEndpoint로 서비스 설정
            .userInfoEndpoint(userInfo -> userInfo
                .userService(kakaoOAuth2UserService)
            )
            .successHandler(customSuccessHandler)
            .failureHandler(customFailureHandler)
        )

//        csrf 비활성화
        .csrf(AbstractHttpConfigurer::disable)
        .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
        .logout(logout -> logout
                .logoutUrl("/api/logout")
                .logoutSuccessHandler(customLogoutSuccessHandler)
                .permitAll()
//            .logoutSuccessUrl("/login?logout")
        )

        .exceptionHandling(conf -> conf
          .authenticationEntryPoint((request, response, authException) -> {
          // 페이지 이동(302) 대신 401 에러 코드 전송
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
          })
        )

        .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)

        // 커스텀 JSON 필터를 UsernamePasswordAuthenticationFilter 위치에 추가
        .addFilterAt(jsonLoginFilter(), org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)

        // CORS 설정
        .cors(cors -> cors.configurationSource(corsConfigurationSource));

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

}