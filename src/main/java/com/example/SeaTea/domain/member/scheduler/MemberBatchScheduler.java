package com.example.SeaTea.domain.member.scheduler;

import com.example.SeaTea.domain.member.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Slf4j
@Component
@AllArgsConstructor
public class MemberBatchScheduler {

  private final MemberRepository memberRepository;

  // 새벽 3시에 14일 지난 탈퇴 회원 데이터 영구 삭제
  @Scheduled(cron = "0 0 3 * * *")
  @Transactional
  public void cleanupOldWithdrawnMembers() {
    log.info("탈퇴 회원 데이터 정리 스케줄러 시작");

    // 14일 뒤에 삭제되도록 설정
    LocalDateTime threshold = LocalDateTime.now().minusDays(14);

    try {
      memberRepository.deleteByDeletedAtBefore(threshold);
      log.info("30일 경과 탈퇴 회원 데이터 정리 완료 (기준일: {})", threshold);
    } catch (Exception e) {
      log.error("탈퇴 회원 데이터 정리 중 오류 발생: {}", e.getMessage());
    }
  }

}
