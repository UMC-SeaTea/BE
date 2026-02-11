package com.example.SeaTea.global.auth.repository;

import com.nimbusds.oauth2.sdk.token.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
  Optional<RefreshToken> findByToken(String token);
  Optional<RefreshToken> findByUserId(Long userId);
  void deleteByUserId(Long userId);
}
