# 프로젝트: 관동 도감 (pokedex-rag)
@~/.claude/claude-stacks/springboot.md

## 프로젝트 구체값
- 도메인 = 관동(1세대) 포켓몬 도감 + RAG 자연어 질의응답
- DB = PostgreSQL 16 + pgvector (로컬: Docker Compose / 프로덕션: Neon 무료)
- LLM·임베딩 = Google Gemini API 무료 티어 (모델 ID는 M3 착수 시 콘솔에서 확인 후 확정)
- 프론트 = Thymeleaf + 바닐라 JS (별도 프론트 레포 없음, 배포 대상 1개)
- 배포 = Render 무료 Web Service (Docker 이미지)
- 인증 = 없음 / 멀티테넌트 = 아님

## 설계 문서
- `docs/PRD.md` — 범위·스택 결정 근거·데이터 소스·마일스톤. **구현 전 반드시 참조**
- `docs/STATUS.md` — 진행 상황, 다음 작업

## 이 프로젝트만의 규칙
- **시크릿은 `.env` 로만 주입.** `.env.example` 에는 키 이름만 남기고 값은 비워 둔다
- 벡터 검색만 `JdbcTemplate` 네이티브 쿼리, 나머지 조회는 JPA (Spring Data JPA 가 vector 타입을 다루지 못함)
- 문서 170건 규모라 **청킹·벡터 인덱스는 넣지 않는다** (PRD §8 근거)
- 포켓몬 IP 관련: 개인 비영리 포트폴리오. 스프라이트는 PokeAPI URL 링크만 사용하고 재배포하지 않는다
