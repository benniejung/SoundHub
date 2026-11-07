# v0.0.2

## 목표
휴대폰 SMS 인증을 도입해, 한 사람이 휴대폰 번호 하나당 계정 하나만 만들 수 있게 합니다.

## 기능 목록
| ID | 기능 | 구분 | 문서 |
|---|---|---|---|
| AUTH-003 | 휴대폰 SMS 인증 (발송/확인) | 신규 | [auth-sms-verification.md](auth-sms-verification.md) |
| AUTH-001 | 회원가입 | 변경 | [auth-signup.md](auth-signup.md) |
| AUTH-002 | 로그인 | 변경 | [auth-login.md](auth-login.md) |
| AUTH-004 | 토큰 재발급 | 변경 없음 | [v0.0.1/auth-token-refresh.md](../v0.0.1/auth-token-refresh.md) |
| AUTH-005 | 로그아웃 | 변경 없음 | [v0.0.1/auth-logout.md](../v0.0.1/auth-logout.md) |
| AUTH-006 | 닉네임 중복 확인 | 제거 | [v0.0.1/auth-nickname-check.md](../v0.0.1/auth-nickname-check.md) |

## v0.0.1 대비 변경
- **신규**: 인증번호 발송(`POST /api/auth/phone/send`), 인증번호 확인(`POST /api/auth/phone/verify`)
- **회원가입**: 휴대폰 인증 후 받은 `signupToken`이 필수, 휴대폰 번호 중복 가입 차단
- **비밀번호 규칙 강화**: 영어 대문자/소문자 각각 필수, 이메일 아이디 포함 금지, 흔한 비밀번호 금지
- **닉네임 중복 확인**: `GET /api/auth/confirm-nickname` API 제거 (서비스 메서드는 남아 있음)
- **로그인**: 필터 방식에서 컨트롤러-서비스 방식으로 변경, 응답 바디를 공통 `BaseResponse` 형식으로 통일

## 제외 범위
- 휴대폰 번호로 로그인, 비밀번호 찾기
- 소셜 로그인
