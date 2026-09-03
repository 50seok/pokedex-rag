# STATUS — 관동 도감 (pokedex-rag)

> 마지막 갱신: 2026-09-03

## 인프라

| 영역 | 선택 | 상태 |
|---|---|---|
| 앱 호스팅 | Render 무료 Web Service | 미설정 |
| DB | Neon 무료 (PostgreSQL + pgvector) | 미설정 |
| 로컬 DB | Docker Compose (`pgvector/pgvector:pg16`) | 미설정 |
| LLM·임베딩 | Google Gemini API 무료 티어 | 키 미발급 |
| CI | 미정 | — |

> Spring Boot 버전 정정: PRD 작성 시점엔 3.x를 가정했으나 `start.spring.io` 기준 3.x가 지원 범위 밖으로 빠져 **4.1.1**로 진행함.

## 마지막 머지 PR

없음 (M1 PR 생성 예정, 머지 후 갱신)

## 다음 작업

**P0**
- [x] Spring Boot 프로젝트 생성 (Java 21, Gradle, 4.1.1) — 2026-09-03
- [x] M1 — PokeAPI 수집기로 포켓몬 151건 확보 (`data/pokemon.json`) — 2026-09-03
- [x] M1 — 마을 10건 · 도장 8건 작성, namu.wiki 개별 문서로 교차 검증 (`data/kanto-towns.json`, `data/kanto-gyms.json`) — 2026-09-03

**P1**
- [ ] M2 — 스키마 + pgvector + 임베딩 적재
- [ ] M2 — 질문 10개로 검색 정확도 눈검증
- [ ] M3 — Gemini 모델 ID 확정 (AI Studio 콘솔에서 직접 확인)
- [ ] M3 — `/api/chat` (검색 → 프롬프트 → 생성 → 출처 반환)

**P2**
- [ ] M4 — 도감 페이지 + 챗 UI
- [ ] M5 — Docker · Render · Neon 배포
- [ ] M5 — 슬립 방지 핑 구성

## 알려진 이슈

| 이슈 | 영향 | 대응 |
|---|---|---|
| Render 무료 512MB RAM | Spring Boot 기동 실패 가능 | `-Xmx320m` 등 JVM 튜닝, 의존성 최소화 |
| Render 15분 유휴 시 슬립 | 첫 접속 약 1분 대기 | 10분 간격 외부 핑 (월 744h < 750h 한도) |
| Gemini 한국어 임베딩 품질 미검증 | 검색 정확도 저하 가능 | M2 완료 시 질문 10개로 확인 후 대안 검토 |
| Gemini 무료 티어 입력 데이터 학습 사용 | 없음 (공개 정보만 다룸) | — |
