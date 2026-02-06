# 🌊 SeaTea (Backend)

**9th UMC Web**

현대인을 위한 맞춤형 휴식 공간 추천 서비스 (Backend)  
Tea Tasting Note에서 영감을 받아,  
사용자 상태에 맞는 휴식 유형을 진단하고 공간·경험 데이터를 제공합니다.

---

## 📌 Project Overview

**SeaTea Backend**는

- 사용자의 **상세 진단 / 간단 진단**을 통해
- 현재 상태를 **8가지 휴식 유형 (Tasting Note Type)** 으로 분류하고
- 이에 기반한 **공간 추천 및 진단 이력 관리 API**를 제공합니다.

Spring Boot 기반의 **REST API 서버**로,  
프론트엔드 및 모바일 클라이언트와의 연동을 목표로 합니다.

---

## 👥 Contributors

<table>
  <tr>
    <td align="center">
      <a href="https://github.com/heebindev">
        <img src="https://avatars.githubusercontent.com/heebindev?v=4" width="120px;" alt="profile"/>
        <br />
        <sub><b>윤희빈</b></sub>
      </a>
      <br />
      <span>Backend Lead · Diagnosis</span>
    </td>
    <td align="center">
      <a href="https://github.com/trymimi">
        <img src="https://avatars.githubusercontent.com/trymimi?v=4" width="120px;" alt="profile"/>
        <br />
        <sub><b>김미미</b></sub>
      </a>
      <br />
      <span>Backend · Place</span>
    </td>
    <td align="center">
      <a href="https://github.com/lsw71311">
        <img src="https://avatars.githubusercontent.com/lsw71311?v=4" width="120px;" alt="profile"/>
        <br />
        <sub><b>이성원</b></sub>
      </a>
      <br />
      <span>Backend · Infra</span>
    </td>
    <td align="center">
      <a href="https://github.com/seojam03">
        <img src="https://avatars.githubusercontent.com/seojam03?v=4" width="120px;" alt="profile"/>
        <br />
        <sub><b>서재민</b></sub>
      </a>
      <br />
      <span>Backend · Auth</span>
    </td>
  </tr>
</table>

---

## 🛠 Tech Stack

| Category | Stack |
|--------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.x |
| ORM | Spring Data JPA (Hibernate) |
| Database | MySQL, H2 (Test) |
| Authentication | Spring Security, JWT |
| Build Tool | Gradle |
| API Docs | Swagger (springdoc-openapi) |
| Validation | Jakarta Validation |
| Infra | AWS EC2, RDS |
| CI/CD | GitHub Actions |
| Collaboration | Git, GitHub, Notion, Discord |

---

## 📌 Commit Convention

### Format

#이슈번호 [타입] 작업명

### Type List
- **[FEAT]** : 새로운 기능 구현  
- **[FIX]** : 버그 및 오류 해결  
- **[REFACTOR]** : 리팩토링  
- **[MOD]** : 코드 수정  
- **[ADD]** : 라이브러리 추가, 파일 생성  
- **[DEL]** : 코드/파일 삭제  
- **[CHORE]** : 설정 변경, 잡일  
- **[DOCS]** : 문서 수정  

---

## 🌿 Git Flow Strategy

### Branch Types
- **main**  
  → 배포 가능한 최종 코드

- **dev**  
  → 개발 중인 기능 통합 브랜치

- **feat/***  
  → 기능 단위 개발 브랜치

- **fix/***, **refactor/***  
  → 오류 수정 / 구조 개선 브랜치

---

## 🔄 Workflow

1. Issue 생성  
2. Branch 생성  
3. 기능 구현 및 커밋  
4. Pull Request 생성  
5. Code Review  
6. 리뷰 반영  
7. Merge (`dev`)  
8. Pull 최신화  
9. 브랜치 삭제
