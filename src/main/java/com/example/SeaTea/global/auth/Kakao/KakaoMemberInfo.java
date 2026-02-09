package com.example.SeaTea.global.auth.Kakao;

import java.util.Map;

public class KakaoMemberInfo {
  private Map<String, Object> attributes;

  public KakaoMemberInfo(Map<String, Object> attributes) {
    this.attributes = attributes;
  }

  public String getProviderId() {
    // 예외처리(null 처리)
    Object id = attributes.get("id");
    return id != null ? id.toString() : null;

//    return attributes.get("id").toString();
  }

  public String getEmail() {
    // 예외처리(null 처리)
    Object accountObj =  attributes.get("kakao_account");

    if(accountObj instanceof Map<?, ?> account) {
      Object email = account.get("email");
      return email != null ? email.toString() : null;
    }
    return null;

//    Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
//    if(account != null) {
//      return account.get("email").toString();
//    } else {
//      return null;
//    }

//    return (String) account.get("email");
  }

  public String getName() {
    // 예외처리(null 처리)
    Object accountObj =  attributes.get("kakao_account");

    if(accountObj instanceof Map<?, ?> account) {
      Object profileObj = account.get("profile");
      if (profileObj instanceof Map<?, ?> profile) {
        Object nickname = profile.get("nickname");
        return nickname != null ? nickname.toString() : null;
      }
    }
    return null;

//    Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
//    if(account == null) { return null; }
//    Map<String, Object> profile = (Map<String, Object>) account.get("profile");
//    if(profile == null) {
//      return null;
//    } else {
//      return  profile.get("nickname").toString();
//    }

//    return (String) profile.get("nickname");
  }

}
