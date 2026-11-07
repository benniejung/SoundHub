# [ADMIN-001] 운영 대시보드

| 항목 | 내용 |
|---|---|
| 버전 | v0.0.1 |
| 구분 | MVP 1 — 운영자 백오피스 |
| 상태 | 기획 |

## 1. 개요
- **목적**: 운영자가 오늘의 업로드, 검수, 파이프라인 상황을 한 화면에서 파악한다.
- **사용자 스토리**: "운영자로서, 출근하자마자 처리할 일과 문제 상황을 한눈에 보고 싶다."

## 2. 범위
- 포함: 오늘 지표, 이상 상태/실패 작업 알림, 지표 클릭 시 관련 목록으로 이동
- 제외: Kafka Consumer Lag/DLQ 지표, 기간별 통계 (README "나중에 확장")

## 3. 기능 요구사항
| ID | 지표/기능 | 정의 | 클릭 시 이동 | 우선순위 |
|---|---|---|---|---|
| ADMIN-001-1 | 오늘 업로드 수 | 오늘 생성된 Track 수 | 파이프라인 목록 (기간=오늘) | Must |
| ADMIN-001-2 | 검수 대기 | 현재 `REVIEW_PENDING` 수 (보류 포함) | 검수 대기열 | Must |
| ADMIN-001-3 | 오늘 승인 | 오늘 `APPROVED` 결정 수 | 검수 이력 (승인) | Must |
| ADMIN-001-4 | 오늘 반려 | 오늘 `REJECTED` + `REVISION_REQUESTED` 결정 수 | 검수 이력 (반려) | Must |
| ADMIN-001-5 | 전체 작업 처리 수 | 오늘 `COMPLETED`된 파이프라인 작업 수 | 파이프라인 목록 (완료) | Must |
| ADMIN-001-6 | 실패 작업 알림 | 현재 `FAILED` 작업 수, 단계별 내역 | 파이프라인 목록 (실패) | Must |
| ADMIN-001-7 | 이상 상태 알림 | `PROCESSING`이 30분 넘게 그대로인 작업 수 | 파이프라인 목록 (멈춤) | Must |

## 4. 비즈니스 규칙 / 정책
- "오늘"은 KST 00:00부터 현재까지다.
- 실패/이상 알림은 0건이면 숨기고, 1건 이상이면 강조 표시한다.
- 화면은 1분마다 자동 새로고침한다. (클라이언트 폴링)

## 5. 처리 흐름
```mermaid
flowchart LR
    A[대시보드 조회] --> B[지표 집계 쿼리]
    B --> C[지표 카드]
    C -- 클릭 --> D[필터가 적용된 목록 화면]
```

## 6. API 명세 (제안) · `ADMIN` 전용
`GET /api/admin/dashboard`

```json
{
  "date": "2026-11-07",
  "todayUploads": 24,
  "reviewPending": 9,
  "todayApproved": 15,
  "todayRejected": 3,
  "todayCompletedJobs": 96,
  "failedJobs": { "total": 2, "byStage": { "TRANSCODING": 1, "AI_ANALYSIS": 1 } },
  "stuckJobs": 1
}
```

**에러**: 403 (`ADMIN`이 아님)

## 7. 데이터
- 조회만 함: `tracks`, `review_decisions`, `pipeline`

## 8. 예외 및 엣지 케이스
- 자정 직후에는 오늘 지표가 0이어도 정상이다.

## 9. 비기능 요구사항
- 응답 500ms 이내. 느려지면 1분 캐시를 둔다.
- 집계 쿼리에 `created_at`, `status` 인덱스 사용

## 10. 인수 조건
- [ ] Given 오늘 업로드 3건, 어제 2건 / When 대시보드 조회 / Then todayUploads=3
- [ ] Given FAILED 작업 2건 (트랜스코딩 1, AI 분석 1) / When 조회 / Then failedJobs.total=2, 단계별 1건씩
- [ ] Given 40분째 PROCESSING인 작업 1건 / When 조회 / Then stuckJobs=1
- [ ] Given USER 토큰 / When 조회 / Then 403

## 11. 미결 사항
- 없음

## 12. 변경 이력
| 버전 | 내용 |
|---|---|
| v0.0.1 | 최초 작성 |
