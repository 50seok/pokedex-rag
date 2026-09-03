# STATUS — 관동 도감 (pokedex-rag)

> 마지막 갱신: 2026-09-04

## 인프라

| 영역 | 선택 | 상태 |
|---|---|---|
| 앱 호스팅 | Render 무료 Web Service | 미설정 |
| DB | Neon 무료 (PostgreSQL + pgvector) | 미설정 |
| 로컬 DB | Docker Compose (`pgvector/pgvector:pg16`) | 검증 완료 (169건 적재+검색 확인, 2026-09-04) |
| LLM·임베딩 | Google Gemini API 무료 티어 | 키 발급 완료 (`.env`) |
| CI | [pr-gate](https://github.com/50seok/pokedex-rag/tree/dev/.github/workflows/pr-gate.yml) (자체 제작, Claude CLI로 diff P1~P3 리뷰) | 관찰 모드로 설치됨 — `dev` 베이스, auto-merge·required check 없음 |

> Spring Boot 버전 정정: PRD 작성 시점엔 3.x를 가정했으나 `start.spring.io` 기준 3.x가 지원 범위 밖으로 빠져 **4.1.1**로 진행함.
>
> 브랜치 모델: **M3부터 PR base는 `main`이 아니라 `dev`.** pr-gate가 `dev`로 향하는 PR만 리뷰하기 때문(원래 설계 유지 — main 자동 머지는 M5 배포에 영향을 주므로 제외). `dev`→`main` 승격은 사람이 직접 한다. M2(PR #3)는 이 규칙 적용 전이라 예외적으로 `main`에 바로 머지됨.

## 마지막 머지 PR

[#3 — feat: M2 스키마 + pgvector + 임베딩 적재](https://github.com/50seok/pokedex-rag/pull/3) (Closes #2) — 2026-09-04

> PR #3 머지 후 실제 `--app.ingest.enabled=true` 로 처음 돌려보고서야 드러난 버그 3건을 main에 직접 핫픽스로 반영함(트리비얼 수정, 별도 PR 없이 진행):
> - `a8489c0` — Flyway 자동설정이 Boot 4에서 `spring-boot-starter-flyway` 없이는 아예 안 켜짐(마이그레이션 미실행) → 스타터 추가
> - `a8489c0` — Boot 4 기본 Jackson이 `tools.jackson`으로 바뀌면서 `com.fasterxml.jackson.databind.ObjectMapper` 빈이 자동 등록 안 됨 → `DataIngestRunner`가 직접 생성하도록 변경
> - `8721a1a` — Gemini `embedContent`가 `embedContentConfig.outputDimensionality=768` 요청을 무시하고 3072차원 그대로 응답하는 사례 실측 → 응답을 클라이언트에서 앞 768개로 잘라 사용
>
> `app.ingest.enabled=true`가 기본 false로 게이팅돼 있어 빌드/테스트(CI)에서는 `DataIngestRunner` 빈이 인스턴스화조차 안 돼 세 버그 다 안 잡혔다. **`/api/chat`(M3)이 재사용할 `GeminiEmbeddingService`·`DocumentRepository`는 이 수정을 거쳐 실제 검증까지 끝난 상태.**

## 다음 작업

**P0**
- [x] Spring Boot 프로젝트 생성 (Java 21, Gradle, 4.1.1) — 2026-09-03
- [x] M1 — PokeAPI 수집기로 포켓몬 151건 확보 (`data/pokemon.json`) — 2026-09-03
- [x] M1 — 마을 10건 · 도장 8건 작성, namu.wiki 개별 문서로 교차 검증 (`data/kanto-towns.json`, `data/kanto-gyms.json`) — 2026-09-03
- [x] M2 — 스키마 + pgvector + 임베딩 적재 (Flyway, Pokemon/Town/Gym 엔티티, DocumentRepository, GeminiEmbeddingService, DataIngestRunner) — 2026-09-04
- [x] M2 — 질문 10개로 검색 정확도 눈검증 — 169건 적재 완료, 10문항 중 5개 top-1 정답 일치·나머지도 관련 카테고리 정상 검색(2개는 질문 자체가 관동 151종/지명 범위 밖이라 구조적으로 정답 불가) — 2026-09-04, **M2 완료**

**P1**
- [ ] M3 — Gemini 모델 ID 확정 (AI Studio 콘솔에서 직접 확인) — 임베딩은 `gemini-embedding-001`로 확정됨(M2), 채팅 생성 모델만 남음
- [ ] M3 — `/api/chat` (검색 → 프롬프트 → 생성 → 출처 반환) — `DocumentRepository.searchTopK()`·`GeminiEmbeddingService` 재사용. **PR base는 `dev`**(위 브랜치 모델 참고)

**P2**
- [ ] M4 — 도감 페이지 + 챗 UI
- [ ] M5 — Docker · Render · Neon 배포
- [ ] M5 — 슬립 방지 핑 구성

## 알려진 이슈

| 이슈 | 영향 | 대응 |
|---|---|---|
| Render 무료 512MB RAM | Spring Boot 기동 실패 가능 | `-Xmx320m` 등 JVM 튜닝, 의존성 최소화 |
| Render 15분 유휴 시 슬립 | 첫 접속 약 1분 대기 | 10분 간격 외부 핑 (월 744h < 750h 한도) |
| Gemini `embedContent`가 `outputDimensionality` 요청을 무시할 수 있음 | 서버가 768 대신 3072차원 응답 → pgvector insert 실패 | `GeminiEmbeddingService`에서 항상 앞 768개로 클라이언트 잘라내기 적용 완료(해결됨) |
| Gemini 무료 티어 입력 데이터 학습 사용 | 없음 (공개 정보만 다룸) | — |
| Gemini API 키가 속한 프로젝트의 결제(prepay) 크레딧 소진 시 429 | 임베딩/채팅 호출 전체 실패 | AI Studio(https://ai.studio/projects)에서 프로젝트별 결제 상태 확인. 현재 trpg-gm과 키 공유 중이라 두 프로젝트 사용량이 합산됨 |
