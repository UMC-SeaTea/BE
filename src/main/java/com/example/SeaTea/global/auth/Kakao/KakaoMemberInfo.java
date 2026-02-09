package com.example.SeaTea.global.auth.Kakao;

import java.util.Map;

public class KakaoMemberInfo {
  private Map<String, Object> attributes;

  public KakaoMemberInfo(Map<String, Object> attributes) {
    this.attributes = attributes;
  }

  public String getProviderId() {
    return attributes.get("id").toString();
  }

  public String getEmail() {
    Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
    return (String) account.get("email");
  }

  public String getName() {
    Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
    Map<String, Object> profile = (Map<String, Object>) account.get("profile");
    return (String) profile.get("nickname");
  }

}
