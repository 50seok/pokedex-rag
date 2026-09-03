# 관동 도감 (Kanto Pokédex RAG)

관동 지방 포켓몬·마을·도장 정보를 **자연어로 물어보면 근거 문서와 함께 답해주는** 도감 사이트입니다.

> 예) "관동에 물타입 도장 있어?" → 해당 체육관·관장 정보 + 출처 링크

## 왜 만들었나

일반적인 도감 사이트는 원하는 정보를 찾으려면 목록을 뒤져야 합니다. 이 프로젝트는 같은 데이터를 **RAG(검색 증강 생성)** 로 감싸서 질문으로 접근할 수 있게 하고, 답변마다 **출처 문서를 함께 표시**해 지어낸 답이 아님을 확인할 수 있게 합니다.

## 기술 스택

| 영역 | 사용 |
|---|---|
| 백엔드 | Java 21, Spring Boot 4.1.1, Gradle |
| DB | PostgreSQL 16 + pgvector |
| LLM · 임베딩 | Google Gemini API |
| 프론트 | Thymeleaf + 바닐라 JS |
| 배포 | Docker → Render / Neon |

선택 근거와 기각한 대안은 [`docs/PRD.md`](docs/PRD.md)에 정리했습니다.

## 동작 방식

```
질문 → 임베딩 → pgvector 유사도 검색(top-5) → 프롬프트 조립 → Gemini → 답변 + 출처
```

문서는 총 약 170건(포켓몬 151 · 마을 10 · 도장 8)이며, 건당 200~500자로 짧아 **청킹하지 않습니다.**

## 실행 방법

```bash
# 1. 환경변수 설정
cp .env.example .env      # 편집해서 GEMINI_API_KEY 등을 채웁니다

# 2. 로컬 DB 기동
docker compose up -d

# 3. 애플리케이션 실행
./gradlew bootRun

# 4. (최초 1회) 데이터 적재 — DB에 포켓몬·마을·도장 저장 + Gemini 임베딩 + 검색 검증
./gradlew bootRun --args='--app.ingest.enabled=true'
```

Gemini API 키는 [Google AI Studio](https://aistudio.google.com/apikey)에서 무료로 발급받을 수 있습니다.

## 진행 상황

M2(스키마·pgvector·임베딩 적재)까지 완료. 상세는 [`docs/STATUS.md`](docs/STATUS.md)를 참고하세요.

## 고지

개인 학습·포트폴리오 목적의 비영리 프로젝트입니다. 포켓몬 관련 명칭과 이미지의 권리는 닌텐도 / Game Freak / 포켓몬 컴퍼니에 있습니다. 포켓몬 이미지는 [PokeAPI](https://pokeapi.co/)가 제공하는 URL을 링크로만 사용하며 재배포하지 않습니다.
