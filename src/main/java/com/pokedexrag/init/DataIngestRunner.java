package com.pokedexrag.init;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pokedexrag.dto.DocumentSearchResult;
import com.pokedexrag.entity.Gym;
import com.pokedexrag.entity.Pokemon;
import com.pokedexrag.entity.Town;
import com.pokedexrag.init.DocumentTextBuilder.GymJson;
import com.pokedexrag.init.DocumentTextBuilder.PokemonJson;
import com.pokedexrag.init.DocumentTextBuilder.TownJson;
import com.pokedexrag.repository.DocumentRepository;
import com.pokedexrag.repository.GymRepository;
import com.pokedexrag.repository.PokemonRepository;
import com.pokedexrag.repository.TownRepository;
import com.pokedexrag.service.GeminiEmbeddingService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

/**
 * data/*.json (포켓몬 151·마을 10·도장 8)을 DB에 적재하고, 각 문서를 Gemini로 임베딩해
 * document 테이블에 넣는다. 마지막으로 고정 질문 10개로 검색 결과를 콘솔에 출력해 검증한다.
 * app.ingest.enabled=true 일 때만 실행된다 (기본 false — 평소 bootRun엔 영향 없음).
 */
@Component
@ConditionalOnProperty(name = "app.ingest.enabled", havingValue = "true")
public class DataIngestRunner implements ApplicationRunner {

    private static final long EMBED_DELAY_MS = 200;

    private static final List<String> VERIFICATION_QUESTIONS = List.of(
            "피카츄는 어디서 잡을 수 있어?",
            "관동에 물 타입 도장이 있어?",
            "이상해씨는 어떤 타입이야?",
            "레드가 여행을 시작하는 마을은 어디야?",
            "불꽃 타입 스타팅 포켓몬은 누구야?",
            "세이지시티 체육관장은 누구야?",
            "고오스는 어떤 포켓몬이야?",
            "관동 지방에는 도장이 몇 개 있어?",
            "무당벌레 모양 포켓몬 이름이 뭐야?",
            "야돈은 어떤 특징이 있어?"
    );

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PokemonRepository pokemonRepository;
    private final TownRepository townRepository;
    private final GymRepository gymRepository;
    private final DocumentRepository documentRepository;
    private final GeminiEmbeddingService embeddingService;

    public DataIngestRunner(PokemonRepository pokemonRepository, TownRepository townRepository,
                             GymRepository gymRepository, DocumentRepository documentRepository,
                             GeminiEmbeddingService embeddingService) {
        this.pokemonRepository = pokemonRepository;
        this.townRepository = townRepository;
        this.gymRepository = gymRepository;
        this.documentRepository = documentRepository;
        this.embeddingService = embeddingService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<PokemonJson> pokemons = objectMapper.readValue(Path.of("data", "pokemon.json").toFile(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, PokemonJson.class));
        List<TownJson> towns = objectMapper.readValue(Path.of("data", "kanto-towns.json").toFile(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, TownJson.class));
        List<GymJson> gyms = objectMapper.readValue(Path.of("data", "kanto-gyms.json").toFile(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, GymJson.class));

        pokemonRepository.saveAll(pokemons.stream().map(this::toEntity).toList());
        townRepository.saveAll(towns.stream().map(this::toEntity).toList());
        gymRepository.saveAll(gyms.stream().map(this::toEntity).toList());
        System.out.println("DB 적재 완료: 포켓몬 %d, 마을 %d, 도장 %d".formatted(pokemons.size(), towns.size(), gyms.size()));

        documentRepository.deleteAll();
        int total = pokemons.size() + towns.size() + gyms.size();
        int done = 0;

        for (PokemonJson p : pokemons) {
            ingest("pokemon", p.id(), DocumentTextBuilder.pokemonTitle(p), DocumentTextBuilder.pokemonContent(p));
            done++;
            System.out.println("임베딩 적재: %d/%d".formatted(done, total));
        }
        for (TownJson t : towns) {
            ingest("town", t.id(), DocumentTextBuilder.townTitle(t), DocumentTextBuilder.townContent(t));
            done++;
            System.out.println("임베딩 적재: %d/%d".formatted(done, total));
        }
        for (GymJson g : gyms) {
            ingest("gym", g.id(), DocumentTextBuilder.gymTitle(g), DocumentTextBuilder.gymContent(g));
            done++;
            System.out.println("임베딩 적재: %d/%d".formatted(done, total));
        }

        verify();
    }

    private void ingest(String sourceType, int sourceId, String title, String content) throws InterruptedException {
        float[] embedding = embeddingService.embed(content);
        documentRepository.insert(sourceType, sourceId, title, content, embedding);
        Thread.sleep(EMBED_DELAY_MS);
    }

    private void verify() {
        System.out.println("\n===== 검증 질의 10건 =====");
        for (String question : VERIFICATION_QUESTIONS) {
            float[] embedding = embeddingService.embed(question);
            List<DocumentSearchResult> results = documentRepository.searchTopK(embedding, 3);
            System.out.println("\nQ: " + question);
            for (DocumentSearchResult r : results) {
                System.out.println("  - [%.4f] %s (%s#%d)".formatted(r.distance(), r.title(), r.sourceType(), r.sourceId()));
            }
        }
    }

    private Pokemon toEntity(PokemonJson p) {
        return Pokemon.builder()
                .id(p.id())
                .nameKo(p.nameKo())
                .genusKo(p.genusKo())
                .types(p.types())
                .hp(p.stats().hp())
                .attack(p.stats().attack())
                .defense(p.stats().defense())
                .specialAttack(p.stats().specialAttack())
                .specialDefense(p.stats().specialDefense())
                .speed(p.stats().speed())
                .spriteUrl(p.spriteUrl())
                .flavorTextKo(p.flavorTextKo())
                .build();
    }

    private Town toEntity(TownJson t) {
        return Town.builder()
                .id(t.id())
                .nameKo(t.nameKo())
                .nameEn(t.nameEn())
                .description(t.description())
                .notablePlaces(t.notablePlaces())
                .build();
    }

    private Gym toEntity(GymJson g) {
        return Gym.builder()
                .id(g.id())
                .townId(g.townId())
                .order(g.order())
                .leaderKo(g.leaderKo())
                .type(g.type())
                .badgeKo(g.badgeKo())
                .description(g.description())
                .build();
    }
}
