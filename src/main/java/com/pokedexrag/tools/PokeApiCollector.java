package com.pokedexrag.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PokeAPI에서 관동(1세대) 포켓몬 151종의 한국어 정보를 수집해 data/pokemon.json 으로 저장한다.
 * 일회성 배치라 Spring 컨텍스트 없이 순수 main()으로 실행한다 (./gradlew collectPokemonData).
 */
public final class PokeApiCollector {

    private static final String BASE_URL = "https://pokeapi.co/api/v2";
    private static final int KANTO_COUNT = 151;
    private static final Path OUTPUT_PATH = Path.of("data", "pokemon.json");
    private static final long REQUEST_DELAY_MS = 100;

    // 관동(1세대) 진화 아이템 5종 고정 매핑
    private static final Map<String, String> ITEM_LABELS = Map.of(
            "moon-stone", "문의돌",
            "fire-stone", "불꽃의돌",
            "thunder-stone", "번개의돌",
            "water-stone", "물의돌",
            "leaf-stone", "리프의돌"
    );

    // pokemon-habitat 리소스는 한국어 이름이 없어 9개 슬러그를 정적 매핑한다
    private static final Map<String, String> HABITAT_LABELS = Map.ofEntries(
            Map.entry("cave", "동굴"),
            Map.entry("forest", "숲"),
            Map.entry("grassland", "초원"),
            Map.entry("mountain", "산"),
            Map.entry("rare", "희귀"),
            Map.entry("rough-terrain", "험지"),
            Map.entry("sea", "바다"),
            Map.entry("urban", "도심"),
            Map.entry("waters-edge", "물가")
    );

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, String> colorKoCache = new HashMap<>();
    private final Map<String, String> abilityKoCache = new HashMap<>();
    private final Map<String, JsonNode> evolutionChainCache = new HashMap<>();

    public static void main(String[] args) throws Exception {
        new PokeApiCollector().collect();
    }

    private void collect() throws Exception {
        List<PokemonRecord> pokemons = new ArrayList<>();

        for (int id = 1; id <= KANTO_COUNT; id++) {
            JsonNode species = fetchJson(BASE_URL + "/pokemon-species/" + id);
            sleep();
            JsonNode pokemon = fetchJson(BASE_URL + "/pokemon/" + id);
            sleep();

            pokemons.add(toRecord(id, species, pokemon));
            System.out.println("수집 완료: " + id + "/" + KANTO_COUNT);
        }

        Files.createDirectories(OUTPUT_PATH.getParent());
        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(Files.newBufferedWriter(OUTPUT_PATH, StandardCharsets.UTF_8), pokemons);

        System.out.println("저장 완료: " + OUTPUT_PATH.toAbsolutePath() + " (" + pokemons.size() + "건)");
    }

    private PokemonRecord toRecord(int id, JsonNode species, JsonNode pokemon) throws IOException, InterruptedException {
        String nameKo = findByLanguage(species.get("names"), "ko", "name");
        String genusKo = findByLanguage(species.get("genera"), "ko", "genus");
        String flavorTextKo = findFirstFlavorTextKo(species.get("flavor_text_entries"));

        List<String> types = new ArrayList<>();
        for (JsonNode t : pokemon.get("types")) {
            types.add(t.get("type").get("name").asText());
        }

        Stats stats = new Stats(
                statValue(pokemon, "hp"),
                statValue(pokemon, "attack"),
                statValue(pokemon, "defense"),
                statValue(pokemon, "special-attack"),
                statValue(pokemon, "special-defense"),
                statValue(pokemon, "speed")
        );

        JsonNode officialArtwork = pokemon.at("/sprites/other/official-artwork/front_default");
        String spriteUrl = !officialArtwork.isMissingNode() && !officialArtwork.isNull()
                ? officialArtwork.asText()
                : pokemon.at("/sprites/front_default").asText(null);

        String colorKo = resolveColorKo(species);
        String habitatKo = resolveHabitatKo(species);
        List<String> abilities = resolveAbilities(pokemon);
        EvolutionInfo evolution = resolveEvolution(species, id);

        return new PokemonRecord(id, nameKo, genusKo, types, stats, spriteUrl, flavorTextKo,
                evolution.evolvesFromId(), evolution.evolvesTo(), abilities, habitatKo, colorKo);
    }

    private String resolveColorKo(JsonNode species) throws IOException, InterruptedException {
        String slug = species.at("/color/name").asText();
        if (colorKoCache.containsKey(slug)) {
            return colorKoCache.get(slug);
        }
        JsonNode color = fetchJson(BASE_URL + "/pokemon-color/" + slug);
        sleep();
        String colorKo = findByLanguage(color.get("names"), "ko", "name");
        colorKoCache.put(slug, colorKo);
        return colorKo;
    }

    private String resolveHabitatKo(JsonNode species) {
        JsonNode habitat = species.get("habitat");
        if (habitat == null || habitat.isNull()) {
            return null;
        }
        String slug = habitat.get("name").asText();
        return HABITAT_LABELS.get(slug);
    }

    private List<String> resolveAbilities(JsonNode pokemon) throws IOException, InterruptedException {
        List<String> result = new ArrayList<>();
        for (JsonNode a : pokemon.get("abilities")) {
            String slug = a.at("/ability/name").asText();
            boolean hidden = a.get("is_hidden").asBoolean();
            String abilityKo = resolveAbilityKo(slug);
            result.add(hidden ? abilityKo + "(히든)" : abilityKo);
        }
        return result;
    }

    private String resolveAbilityKo(String slug) throws IOException, InterruptedException {
        if (abilityKoCache.containsKey(slug)) {
            return abilityKoCache.get(slug);
        }
        JsonNode ability = fetchJson(BASE_URL + "/ability/" + slug);
        sleep();
        String abilityKo = findByLanguage(ability.get("names"), "ko", "name");
        abilityKoCache.put(slug, abilityKo);
        return abilityKo;
    }

    private EvolutionInfo resolveEvolution(JsonNode species, int speciesId) throws IOException, InterruptedException {
        String chainUrl = species.at("/evolution_chain/url").asText();
        JsonNode chainRoot = evolutionChainCache.get(chainUrl);
        if (chainRoot == null) {
            JsonNode chain = fetchJson(chainUrl);
            sleep();
            chainRoot = chain.get("chain");
            evolutionChainCache.put(chainUrl, chainRoot);
        }
        EvolutionInfo found = findEvolutionNode(chainRoot, speciesId, null);
        return found != null ? found : new EvolutionInfo(null, List.of());
    }

    private EvolutionInfo findEvolutionNode(JsonNode node, int targetSpeciesId, Integer parentId) {
        int nodeId = idFromUrl(node.at("/species/url").asText());
        if (nodeId == targetSpeciesId) {
            // ponytail: PokeAPI 진화체인엔 후속 세대 진화/역진화(이브이의 에스퍼/블래키 등, 잠만보 앞의 두리코)도 섞여
            // 있어 관동 151종 범위(1~151) 밖 id는 제외한다 — DB에 없는 id라 findById가 404를 던지기 때문
            List<String> evolvesTo = new ArrayList<>();
            for (JsonNode child : node.get("evolves_to")) {
                int childId = idFromUrl(child.at("/species/url").asText());
                if (childId > KANTO_COUNT) {
                    continue;
                }
                evolvesTo.add(childId + ":" + evolutionConditionText(child.get("evolution_details")));
            }
            Integer inRangeParentId = (parentId != null && parentId <= KANTO_COUNT) ? parentId : null;
            return new EvolutionInfo(inRangeParentId, evolvesTo);
        }
        for (JsonNode child : node.get("evolves_to")) {
            EvolutionInfo found = findEvolutionNode(child, targetSpeciesId, nodeId);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    // package-private static: 테스트에서 직접 호출 (인스턴스 상태 없는 순수 함수)
    static String evolutionConditionText(JsonNode evolutionDetails) {
        if (evolutionDetails == null || evolutionDetails.isEmpty()) {
            return "";
        }
        JsonNode detail = evolutionDetails.get(0);
        String trigger = detail.at("/trigger/name").asText();
        return switch (trigger) {
            case "level-up" -> {
                JsonNode minLevel = detail.get("min_level");
                if (minLevel != null && !minLevel.isNull()) {
                    yield minLevel.asInt() + "레벨";
                }
                JsonNode minHappiness = detail.get("min_happiness");
                if (minHappiness != null && !minHappiness.isNull()) {
                    yield "친밀도 " + minHappiness.asInt() + " 이상";
                }
                yield "레벨업";
            }
            case "use-item" -> {
                String itemSlug = detail.at("/item/name").asText();
                yield ITEM_LABELS.getOrDefault(itemSlug, itemSlug) + " 사용";
            }
            case "trade" -> "교환";
            // ponytail: 관동 151종 범위 밖 진화 트리거는 처리 안 함, 필요시 여기 추가
            default -> trigger;
        };
    }

    private int idFromUrl(String url) {
        String trimmed = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        String[] parts = trimmed.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }

    private String findByLanguage(JsonNode entries, String languageCode, String field) {
        for (JsonNode entry : entries) {
            if (languageCode.equals(entry.at("/language/name").asText())) {
                return entry.get(field).asText();
            }
        }
        throw new IllegalStateException("언어 '" + languageCode + "'의 '" + field + "' 항목을 찾을 수 없음");
    }

    private String findFirstFlavorTextKo(JsonNode flavorTextEntries) {
        for (JsonNode entry : flavorTextEntries) {
            if ("ko".equals(entry.at("/language/name").asText())) {
                return entry.get("flavor_text").asText()
                        .replace("\n", " ")
                        .replace("\f", " ")
                        .replaceAll(" {2,}", " ")
                        .trim();
            }
        }
        throw new IllegalStateException("한국어 도감설명을 찾을 수 없음");
    }

    private int statValue(JsonNode pokemon, String statName) {
        for (JsonNode s : pokemon.get("stats")) {
            if (statName.equals(s.at("/stat/name").asText())) {
                return s.get("base_stat").asInt();
            }
        }
        throw new IllegalStateException("스탯 '" + statName + "'을 찾을 수 없음");
    }

    private JsonNode fetchJson(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
        try {
            return doFetch(request);
        } catch (IOException e) {
            System.out.println("재시도: " + url + " (" + e.getMessage() + ")");
            return doFetch(request);
        }
    }

    private JsonNode doFetch(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode() + " for " + request.uri());
        }
        return objectMapper.readTree(response.body());
    }

    private void sleep() throws InterruptedException {
        Thread.sleep(REQUEST_DELAY_MS);
    }

    private record Stats(int hp, int attack, int defense, int specialAttack, int specialDefense, int speed) {
    }

    private record EvolutionInfo(Integer evolvesFromId, List<String> evolvesTo) {
    }

    private record PokemonRecord(
            int id,
            String nameKo,
            String genusKo,
            List<String> types,
            Stats stats,
            String spriteUrl,
            String flavorTextKo,
            Integer evolvesFromId,
            List<String> evolvesTo,
            List<String> abilities,
            String habitatKo,
            String colorKo
    ) {
    }
}
