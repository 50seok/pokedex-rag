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
import java.util.List;

/**
 * PokeAPI에서 관동(1세대) 포켓몬 151종의 한국어 정보를 수집해 data/pokemon.json 으로 저장한다.
 * 일회성 배치라 Spring 컨텍스트 없이 순수 main()으로 실행한다 (./gradlew collectPokemonData).
 */
public final class PokeApiCollector {

    private static final String BASE_URL = "https://pokeapi.co/api/v2";
    private static final int KANTO_COUNT = 151;
    private static final Path OUTPUT_PATH = Path.of("data", "pokemon.json");
    private static final long REQUEST_DELAY_MS = 100;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

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

    private PokemonRecord toRecord(int id, JsonNode species, JsonNode pokemon) {
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

        return new PokemonRecord(id, nameKo, genusKo, types, stats, spriteUrl, flavorTextKo);
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

    private record PokemonRecord(
            int id,
            String nameKo,
            String genusKo,
            List<String> types,
            Stats stats,
            String spriteUrl,
            String flavorTextKo
    ) {
    }
}
