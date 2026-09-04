# STATUS — 관동 도감 (pokedex-rag)

> 마지막 갱신: 2026-09-04 (PR #14 머지)

## 인프라

| 영역 | 선택 | 상태 |
|---|---|---|
| 앱 호스팅 | Render 무료 Web Service | 미설정 |
| DB | Neon 무료 (PostgreSQL + pgvector) | 미설정 |
| 로컬 DB | Docker Compose (`pgvector/pgvector:pg16`) | 검증 완료 (169건 적재+검색 확인, 2026-09-04) |
| LLM·임베딩 | Google Gemini API 무료 티어 | 키 발급 완료 (`.env`) |
| CI | [pr-gate](https://github.com/50seok/pokedex-rag/tree/dev/.github/workflows/pr-gate.yml) (자체 제작, Claude CLI로 diff P1~P3 리뷰) | `dev` 베이스, auto-merge·required check(`review`) 켜짐 (2026-09-04) — P1 없으면 사람 손 없이 dev 머지 |

> Spring Boot 버전 정정: PRD 작성 시점엔 3.x를 가정했으나 `start.spring.io` 기준 3.x가 지원 범위 밖으로 빠져 **4.1.1**로 진행함.
>
> 브랜치 모델: **M3부터 PR base는 `main`이 아니라 `dev`.** pr-gate가 `dev`로 향하는 PR만 리뷰하기 때문(원래 설계 유지 — main 자동 머지는 M5 배포에 영향을 주므로 제외). `dev`→`main` 승격은 사람이 직접 한다. M2(PR #3)는 이 규칙 적용 전이라 예외적으로 `main`에 바로 머지됨.

## 마지막 머지 PR

[#12 — feat: M4 도감 페이지 + 챗 UI](https://github.com/50seok/pokedex-rag/pull/12) (Closes #11) + [#14 — 후속 주석 정정](https://github.com/50seok/pokedex-rag/pull/14) (Closes #13) — 2026-09-04

> Thymeleaf + 바닐라 JS로 프론트 전체를 처음 구성(`templates`/`static`가 비어 있었음). `PokedexService`(Pokemon/Town/Gym 조회)는 REST용 `CustomException`/`ErrorCode`와 분리해 `ResponseStatusException`을 씀 — 뷰 컨트롤러의 404는 HTML 에러 페이지로, `/api/chat`의 에러는 JSON으로 갈리게 하기 위해서다. 이를 위해 `GlobalExceptionHandler`를 `@RestControllerAdvice(annotations = RestController.class)`로 스코프 한정.
>
> **부작용 발견·처리**: 스코프 한정 때문에 `GET /api/chat`처럼 핸들러 메서드가 아예 안 잡히는 405 케이스는 advice가 안 걸려 응답 바디가 표준 포맷을 벗어난다(상태코드는 정확). code-reviewer 독립 검증(Spring `HandlerTypePredicate` 소스 직접 확인) 결과 P3로 판단 — REST 엔드포인트가 `/api/chat` 하나뿐이라 별도 `HandlerExceptionResolver` 추가는 YAGNI. pr-gate는 같은 근본원인을 P2로 봤으나(#13) 별도 리졸버까지는 과하다고 판단해 클래스 주석만 정확히 갱신(PR #14)하는 선에서 종결.
>
> **auto-merge 첫 사용**: PR #10(사용자가 세션 밖에서 직접 설정)부터 `dev`에 required check+auto-merge가 켜져 P1 없으면 사람 개입 없이 자동 머지됨(`gh run watch`로 CI 완료만 기다림). 이 과정에서 **`dev` 베이스 PR은 "Closes #N"이 자동으로 이슈를 안 닫는다는 사실을 뒤늦게 발견**(기본 브랜치 `main`만 자동 동작) — 밀려 있던 #4·#7·#11·#13을 이번에 한꺼번에 수동으로 닫음. 앞으로 매 머지 후 `gh issue close` 수동 호출 필요.

[#8 — feat: M3-2 /api/chat 오케스트레이션](https://github.com/50seok/pokedex-rag/pull/8) (Closes #7) — 2026-09-04

> pr-gate 리뷰 3라운드(#9, 매 라운드 병합 전 처리 후 머지):
> 1차 P2 3건 — Gemini 429/5xx 미방어(`RestClientResponseException` catch 추가), `/api/chat` 질문 길이 제한 없음(`@Size(max=500)`), 검색 0건 테스트 누락
> 2차 P2 1건 — `GlobalExceptionHandler`에 catch-all(`Exception.class`) 핸들러 부재로 DB 예외 등이 비표준 포맷으로 새어나감 → 추가
> 3차 P2 1건 — 방금 추가한 catch-all이 `HttpRequestMethodNotSupportedException`(405) 등 Spring 표준 예외까지 500으로 덮어씀 → `GlobalExceptionHandler`가 `ResponseEntityExceptionHandler`를 상속해 `handleExceptionInternal`만 오버라이드하는 구조로 전환(표준 상태코드는 보존, 바디 포맷만 통일). `ChatService`의 catch 대상도 `RestClientException`(타임아웃 등 `ResourceAccessException` 포함)으로 넓힘
> 최종 재검사: P1 0 · P2 0 · P3 0
>
> `exception`/`controller` 패키지를 이 PR에서 처음 만듦(springboot.md ErrorCode/CustomException/GlobalExceptionHandler 컨벤션 적용) — 앞으로 컨트롤러 추가 시 그대로 재사용.

[#5 — feat: M3-1 GeminiChatService (generateContent 연동)](https://github.com/50seok/pokedex-rag/pull/5) (Closes #4) — 2026-09-04

> pr-gate 리뷰에서 P2 2건 지적(#6, 후속 커밋으로 같은 PR 안에서 처리 후 머지):
> - candidates가 null/빈 리스트인 경우(안전 필터 차단) → `IllegalStateException`으로 명시적 처리
> - candidate는 있으나 content/parts가 빈 경우(SAFETY/RECITATION 부분 차단) → 동일하게 명시적 처리
> - RestClient에 connect/read 타임아웃 미설정 → `RestClientCustomizer` 빈(`config/RestClientTimeoutConfig`)으로 전역 적용(connect 5s/read 30s). 서비스 생성자에서 직접 설정하면 `MockRestServiceServer`가 걸어둔 mock requestFactory를 덮어써 테스트가 실제 네트워크를 타버리는 문제가 있어, Spring 컨텍스트로 빌드되는 프로덕션 빈에만 적용되도록 분리함.
>
> P3 1건(멀티파트 응답 시 첫 part만 사용)은 경미해 조치 없이 종료. `gemini.chat.model` 신규 환경변수 `GEMINI_CHAT_MODEL`(선택, 기본 `gemini-flash-lite-latest`) — `.env.example`에 반영.

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
- [x] M3 — Gemini 모델 ID 확정 — 임베딩 `gemini-embedding-001`(M2), 채팅 `gemini.chat.model`(기본 `gemini-flash-lite-latest`, `GEMINI_CHAT_MODEL`로 override) — 2026-09-04
- [x] M3-1 — `GeminiChatService` (generateContent 연동, candidates 빈 응답 방어, 타임아웃 설정) — 2026-09-04, PR #5
- [x] M3-2 — `/api/chat` 오케스트레이션 (검색 → 프롬프트 → 생성 → 출처 반환) + 컨트롤러/DTO/예외처리 — 2026-09-04, PR #8, **M3 완료**

**P1**
- [x] M4 — 도감 페이지 + 챗 UI (Thymeleaf + 바닐라 JS, 포켓몬/마을/도장 목록·상세 + 챗 UI + 출처 칩 링크) — 2026-09-04, PR #12·#14, **M4 완료**

**P2**
- [ ] M5 — Docker · Render · Neon 배포
- [ ] M5 — 슬립 방지 핑 구성

## 알려진 이슈

| 이슈 | 영향 | 대응 |
|---|---|---|
| Render 무료 512MB RAM | Spring Boot 기동 실패 가능 | `-Xmx320m` 등 JVM 튜닝, 의존성 최소화 |
| Render 15분 유휴 시 슬립 | 첫 접속 약 1분 대기 | 10분 간격 외부 핑 (월 744h < 750h 한도) |
| Gemini `embedContent`가 `outputDimensionality` 요청을 무시할 수 있음 | 서버가 768 대신 3072차원 응답 → pgvector insert 실패 | `GeminiEmbeddingService`에서 항상 앞 768개로 클라이언트 잘라내기 적용 완료(해결됨) |
| Gemini 무료 티어 입력 데이터 학습 사용 | 없음 (공개 정보만 다룸) | — |
| ~~Gemini API 키가 속한 프로젝트의 결제(prepay) 크레딧 소진 시 429~~ (해결됨) | ~~임베딩/채팅 호출 전체 실패~~ | trpg-gm과 처음 분리 발급한 키는 같은 GCP 프로젝트("Default Gemini Project") 소속이라 prepay 잔액을 공유해 429 발생 — RPM/TPM/RPD(Tier 1)는 여유 있었으나 프로젝트 단위 prepay 잔액이 0인 게 원인이었음. **별도 GCP 프로젝트를 새로 만들어 그 안에서 키를 재발급**해 완전히 분리, `generateContent`(채팅)·`embedContent`(임베딩) 둘 다 200 응답 확인 — 2026-09-04 |
