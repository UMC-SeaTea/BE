package com.example.SeaTea.global.auth.entity;

import com.example.SeaTea.domain.member.entity.Member;
import com.example.SeaTea.global.auth.CustomUserDetails;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  private final Key key;
  private final long tokenValidityInMilliseconds = 1000L * 60 * 60 * 2; // 2시간 유효

  // yml로 token 안전하게 가져오기
  public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
//    byte[] keyBytes = Decoders.BASE64.decode(Base64.getEncoder().encodeToString(secretKey.getBytes()));
//    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
//    this.key = Keys.hmacShaKeyFor(keyBytes);
    if (secretKey == null || secretKey.isEmpty()) {
      throw new IllegalArgumentException("SECRET_TOKEN 환경 변수가 설정되지 않았습니다!");
    }

    // 2. 만약 ${SECRET_TOKEN} 문자열이 그대로 들어왔다면 에러 발생
    if (secretKey.equals("${SECRET_TOKEN}")) {
      throw new IllegalArgumentException("환경 변수 치환에 실패했습니다. 설정을 확인하세요.");
    }

    // 3. HMAC SHA 키 생성
    this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
  }

  public String createAccessToken(Authentication authentication) {
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
}
