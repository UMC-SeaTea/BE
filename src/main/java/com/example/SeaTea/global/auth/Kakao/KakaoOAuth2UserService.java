package com.example.SeaTea.global.auth.Kakao;

import com.example.SeaTea.domain.member.entity.Member;
import com.example.SeaTea.domain.member.repository.MemberRepository;
import com.example.SeaTea.global.auth.service.CustomUserDetails;
import com.example.SeaTea.global.auth.enums.Role;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KakaoOAuth2UserService extends DefaultOAuth2UserService {

  private final MemberRepository memberRepository;

  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    System.out.println("Kakao login attempt start...");

    // 카카오에서 가져온 기본 유저 정보들
    OAuth2User oAuth2User = super.loadUser(userRequest);
    // 어느 소셜 서비스인지 확인 (현재 kakao만 연결)
    String registrationId = userRequest.getClientRegistration().getRegistrationId();
    // 카카오 응답 데이터 가공
    KakaoMemberInfo kakaoResponse = new KakaoMemberInfo(oAuth2User.getAttributes());

    Map<String, Object> attributes = oAuth2User.getAttributes();

    String email = kakaoResponse.getEmail();
    String nickname = kakaoResponse.getName();
    String providerId = kakaoResponse.getProviderId();

    if (nickname == null || nickname.trim().isEmpty()) {
      // [Speculation] 닉네임이 없을 경우를 대비한 기본값 설정
      nickname = "KakaoUser_" + attributes.get("id");
    }

    // Member 저장 및 업데이트 로직
    Member member = saveOrUpdate(email, nickname, registrationId, providerId);

    // 세션에 저장할 유저 객체 반환
    return new CustomUserDetails(member, oAuth2User.getAttributes());
//    return new DefaultOAuth2User(
//        Collections.singleton(new SimpleGrantedAuthority(member.getRole().getKey())),
//        oAuth2User.getAttributes(),
//        "id" // 카카오는 id를 식별자로 사용 (yml의 user-name-attribute와 동일해야 함)
//    );
  }

  @Transactional
  public Member saveOrUpdate(String email, String nickname, String registerId, String providerId) {
    return memberRepository.findByEmail(email)
        .map(entity -> entity.updateSocialInfo(registerId, providerId)) // 이미 있으면 이름 업데이트
//        .map(entity -> memberRepository.save(entity.updateSocialInfo(registerId, providerId)))
        .orElseGet(() -> {
          // 신규 가입
              Member newMember = Member.builder()
                  .nickname(nickname)
                  .email(email)
                  .role(Role.ROLE_MEMBER)
                  .registrationId(registerId)
                  .providerId(providerId)
                  .build();

              // 신규 회원임을 표시 (엔티티에 만드신 메서드 호출)
              newMember.markAsNewUser();

              return memberRepository.save(newMember);
            });
  }

}
