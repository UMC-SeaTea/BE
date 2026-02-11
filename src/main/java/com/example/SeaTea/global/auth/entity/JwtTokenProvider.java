package com.example.SeaTea.global.auth.entity;

import com.example.SeaTea.domain.member.entity.Member;
import com.example.SeaTea.domain.member.exception.MemberException;
import com.example.SeaTea.domain.member.exception.code.MemberErrorCode;
import com.example.SeaTea.global.auth.enums.Role;
import com.example.SeaTea.global.auth.service.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  private final Key key;

  // yml로 token 안전하게 가져오기
  public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
//    byte[] keyBytes = Decoders.BASE64.decode(Base64.getEncoder().encodeToString(secretKey.getBytes()));
//    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
//    this.key = Keys.hmacShaKeyFor(keyBytes);
    if (secretKey == null || secretKey.isEmpty()) {
      throw new IllegalArgumentException("SECRET_TOKEN 환경 변수가 설정되지 않았습니다!");
    }

    // 만약 ${SECRET_TOKEN} 문자열이 그대로 들어왔다면 에러 발생
    if (secretKey.equals("${jwt.secret}")) {
      throw new IllegalArgumentException("환경 변수 치환에 실패했습니다. 설정을 확인하세요.");
    }

    // HMAC SHA 키 생성
    byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
    if (keyBytes.length < 32) {
      throw new IllegalArgumentException("JWT secret은 최소 32바이트(256비트) 이상이어야 합니다.");
    }
    this.key = Keys.hmacShaKeyFor(keyBytes);
//    this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
  }

  public String createAccessToken(Authentication authentication) {
    long tokenValidityInMilliseconds = 1000L * 60 * 60 * 2; // 2시간 유효

    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
    Member member = userDetails.getMember();

    Date now = new Date();
    Date validity = new Date(now.getTime() + tokenValidityInMilliseconds);

    return Jwts.builder()
        .setSubject(String.valueOf(member.getId())) // 토큰 주인을 식별할 ID
        .claim("email", member.getEmail())          // 추가 정보
        .claim("role", member.getRole().name())
        .setIssuedAt(now)
        .setExpiration(validity)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public String createRefreshToken(Authentication authentication) {
    long refreshTokenValidityInMilliseconds = 1000L * 60 * 60 * 24 * 14; // 14일

    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

    return Jwts.builder()
        .setSubject(String.valueOf(userDetails.getMember().getId()))
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + refreshTokenValidityInMilliseconds))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  // 토큰에서 회원 정보 추출 (Authentication 객체 생성하여 필터에서 인증 성공 시 SecurityContext에 저장할 객체 생성
  public Authentication getAuthentication(String accessToken) {
    // 토큰 복호화
    Claims claims = parseClaims(accessToken);

    if (claims.get("role") == null) {
      throw new MemberException(MemberErrorCode._NOT_RIGHT);
    }

    // 클레임에서 권한 정보 가져오기
    Collection<? extends GrantedAuthority> authorities =
        Arrays.stream(claims.get("role").toString().split(","))
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

    // Member 엔티티 생성(ID와 Role만 채움/가짜 객체임)
    Member member = Member.builder()
        .id(Long.parseLong(claims.getSubject()))
        .role(Role.valueOf(claims.get("role").toString()))
        .build();

    // CustomUserDetails 생성(DB 조회 없이!)
    CustomUserDetails principal = new CustomUserDetails(member);

    return new UsernamePasswordAuthenticationToken(principal, "", authorities);
  }

  // 토큰 유효성 검증(토큰이 위변조되지 않았는지, 만료되지 않았는지 확인)
  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder()
          .setSigningKey(key)
          .build()
          .parseClaimsJws(token);
      return true;
    } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
      // 서명이 잘못됐거나 토큰 구조가 깨짐
      System.out.println("잘못된 JWT 서명입니다.");
    } catch (ExpiredJwtException e) {
      // 유효 기간 만료
      System.out.println("만료된 JWT 토큰입니다.");
    } catch (UnsupportedJwtException e) {
      // 지원되지 않는 형식의 토큰
      System.out.println("지원되지 않는 JWT 토큰입니다.");
    } catch (IllegalArgumentException e) {
      // 토큰이 비어있거나 잘못됨
      System.out.println("JWT 토큰이 잘못되었습니다.");
    }
    return false;
  }

  // 토큰 복호화 (내부 사용)
  private Claims parseClaims(String accessToken) {
    try {
      return Jwts.parserBuilder()
          .setSigningKey(key)
          .build()
          .parseClaimsJws(accessToken)
          .getBody();
    } catch (ExpiredJwtException e) {
      // 만료된 토큰이라도 클레임 정보는 필요할 수 있음(재발급 시)
      return e.getClaims();
    }
  }
}
