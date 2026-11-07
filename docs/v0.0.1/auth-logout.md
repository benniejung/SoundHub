# [AUTH-005] 로그아웃

| 항목 | 내용 |
|---|---|
| 버전 | v0.0.1 |
| 상태 | 완료 |

## 1. 개요
- **목적**: 로그아웃한 뒤에는 refreshToken으로 accessToken을 다시 받을 수 없게 한다.
- **사용자 스토리**: "로그인한 사용자로서, 공용 기기에서 쓴 뒤 안전하게 로그아웃하고 싶다."

## 2. 범위
- 포함: Redis의 refreshToken 삭제, refreshToken 쿠키 만료
- 제외: 이미 발급된 accessToken 즉시 무효화(블랙리스트), 모든 기기에서 로그아웃

## 3. 기능 요구사항
| ID | 요구사항 | 우선순위 |
|---|---|---|
| AUTH-005-1 | 쿠키의 refreshToken으로 사용자를 찾아 Redis의 refreshToken을 삭제한다 | Must |
| AUTH-005-2 | 응답으로 refreshToken 쿠키를 즉시 만료시킨다 | Must |
| AUTH-005-3 | 토큰이 없거나 유효하지 않아도 오류 없이 성공으로 응답한다 | Must |

## 4. 비즈니스 규칙 / 정책
- 로그아웃은 항상 성공한다. (이미 로그아웃한 상태여도 클라이언트 쿠키는 정리되어야 하므로)
- 이미 발급된 accessToken은 남은 유효시간(최대 15분) 동안 계속 쓸 수 있다.

## 5. 처리 흐름
```mermaid
sequenceDiagram
    participant C as Client
    participant S as Server
    participant R as Redis
    C->>S: POST /api/auth/logout (Cookie: refreshToken)
    alt 토큰이 있고 사용자 ID를 읽을 수 있음
        S->>R: 사용자 ID의 refreshToken 삭제
    else 토큰 없음 / 만료 / 위조
        S->>S: 삭제 생략 (로그만 남김)
    end
    S-->>C: 200 + Set-Cookie: refreshToken=; Max-Age=0
```

## 6. API 명세
`POST /api/auth/logout`

**Request**: 바디 없음, 쿠키 `refreshToken` (없어도 됨)

**Response** `200 OK` (헤더 `Set-Cookie: refreshToken=; Max-Age=0; HttpOnly`)
```json
{
  "isSuccess": true,
  "status": 200,
  "code": "OK_200",
  "message": "호출에 성공하였습니다.",
  "result": null,
  "timeStamp": "2026-11-07 12:00:00"
}
```

**에러**: 없음

## 7. 데이터
- Redis: 사용자 ID의 refreshToken 삭제

## 8. 예외 및 엣지 케이스
- 만료된 refreshToken으로 로그아웃하면 사용자 ID를 읽지 못해 Redis 삭제를 건너뛴다. Redis 값도 같은 시각에 TTL로 사라지므로 문제없다.

## 9. 비기능 요구사항
- 쿠키는 발급할 때와 같은 속성(HttpOnly, Secure, SameSite, Path=/)으로 만료시켜야 브라우저에서 지워진다.

## 10. 인수 조건
- [ ] Given 유효한 refreshToken 쿠키 / When 로그아웃 요청 / Then 200을 반환하고 Redis의 refreshToken이 삭제되며 쿠키가 만료된다
- [ ] Given 로그아웃 후 같은 refreshToken / When 재발급 요청 / Then 401(AUTH_401_1)을 반환한다
- [ ] Given refreshToken 쿠키 없음 / When 로그아웃 요청 / Then 200을 반환하고 쿠키 만료 헤더를 보낸다
- [ ] Given 위조된 refreshToken / When 로그아웃 요청 / Then 200을 반환한다

## 11. 변경 이력
| 버전 | 내용 |
|---|---|
| v0.0.1 | 최초 작성 |
