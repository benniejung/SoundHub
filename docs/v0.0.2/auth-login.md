# [AUTH-002] 로그인

| 항목 | 내용 |
|---|---|
| 버전 | v0.0.2 |
| 상태 | 완료 |
| 이전 버전 | [v0.0.1/auth-login.md](../v0.0.1/auth-login.md) |

v0.0.1의 요구사항, 비즈니스 규칙, 처리 흐름은 그대로 유지합니다. 이 문서에는 바뀐 부분만 적습니다.

## 1. 변경 요약
| 구분 | v0.0.1 | v0.0.2 |
|---|---|---|
| 구현 방식 | 인증 필터 + 성공/실패 핸들러 | 컨트롤러 → 서비스 (`AuthenticationManager`) |
| 성공 응답 바디 | `LoginResponseDto`를 그대로 반환 | 공통 `BaseResponse`로 감싸서 반환 |
| 요청, 에러 코드, refreshToken 쿠키 | — | 변경 없음 |

## 2. API 명세
`POST /api/auth/login`

**Response** `200 OK` (헤더 `Set-Cookie: refreshToken=...; HttpOnly`)
```json
{
  "isSuccess": true,
  "status": 200,
  "code": "OK_200",
  "message": "호출에 성공하였습니다.",
  "result": {
    "accessToken": "eyJ...",
    "email": "user@example.com",
    "nickname": "user_1a2b3c4d",
    "role": "USER"
  },
  "timeStamp": "2026-11-07 12:00:00"
}
```

> 클라이언트는 accessToken을 `result.accessToken`에서 읽어야 합니다. (v0.0.1 클라이언트와 호환되지 않음)

## 3. 인수 조건
v0.0.1의 인수 조건에 아래를 추가합니다.
- [ ] Given 가입된 이메일과 올바른 비밀번호 / When 로그인 요청 / Then 응답의 `result.accessToken`에 토큰이 들어 있다

## 4. 변경 이력
| 버전 | 내용 |
|---|---|
| v0.0.1 | 최초 작성 (필터 방식) |
| v0.0.2 | 컨트롤러-서비스 방식으로 변경, 응답을 `BaseResponse`로 감쌈 |
