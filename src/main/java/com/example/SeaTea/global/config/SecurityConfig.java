package com.example.SeaTea.global.config;

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
import org.springframework.web.cors.CorsConfigurationSource;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

  private final CustomSuccessHandler customSuccessHandler;
  private final CustomFailureHandler customFailureHandler;
  private final CustomLogoutSuccessHandler customLogoutSuccessHandler;
  private final CorsConfigurationSource corsConfigurationSource;

  private final String[] allowUris = {
      // Swagger 허용
      "/api/login",
      "/api/sign-up",
      "/swagger-ui/**",
      "/swagger-resources/**",
      "/v3/api-docs/**",
      "/error",

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
//        csrf 비활성화
        .csrf(AbstractHttpConfigurer::disable)
        .logout(logout -> logout
                .logoutUrl("/api/logout")
                .logoutSuccessHandler(customLogoutSuccessHandler)
                .permitAll()
//            .logoutSuccessUrl("/login?logout")
        );

    // CORS 설정
    http.cors(cors -> cors.configurationSource(corsConfigurationSource));

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