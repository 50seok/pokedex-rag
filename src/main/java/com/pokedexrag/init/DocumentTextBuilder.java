package com.pokedexrag.init;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * data/*.json 레코드를 RAG document의 title/content로 조립하는 순수 함수 모음.
 * JSON 파싱용 record도 여기 함께 둔다 (DataIngestRunner와 테스트에서 공유).
 */
final class DocumentTextBuilder {

    private DocumentTextBuilder() {
    }

    static String pokemonTitle(PokemonJson p) {
        return "%s (%s)".formatted(p.nameKo(), p.genusKo());
    }

    /**
     * @param nameById 진화 대상 id -> 한글 이름 (관동 151종 전체). 챗봇 답변에 "2번"이 아닌 실제 이름이 나오게 한다.
     */
    static String pokemonContent(PokemonJson p, Map<Integer, String> nameById) {
        StringBuilder sb = new StringBuilder();
        sb.append("이름: %s\n분류: %s\n타입: %s\n종족값: HP %d 공격 %d 방어 %d 특공 %d 특방 %d 스피드 %d\n설명: %s".formatted(
                p.nameKo(), p.genusKo(), String.join(", ", p.types()),
                p.stats().hp(), p.stats().attack(), p.stats().defense(),
                p.stats().specialAttack(), p.stats().specialDefense(), p.stats().speed(),
                p.flavorTextKo()));
        if (p.abilities() != null && !p.abilities().isEmpty()) {
            sb.append("\n특성: ").append(String.join(", ", p.abilities()));
        }
        if (p.habitatKo() != null || p.colorKo() != null) {
            sb.append("\n서식지/색상: %s/%s".formatted(
                    p.habitatKo() != null ? p.habitatKo() : "알 수 없음",
                    p.colorKo() != null ? p.colorKo() : "알 수 없음"));
        }
        if ((p.evolvesTo() != null && !p.evolvesTo().isEmpty()) || p.evolvesFromId() != null) {
            sb.append("\n진화: ").append(evolutionSummary(p, nameById));
        }
        return sb.toString();
    }

    private static String evolutionSummary(PokemonJson p, Map<Integer, String> nameById) {
        List<String> parts = new ArrayList<>();
        if (p.evolvesFromId() != null) {
            parts.add(nameOf(p.evolvesFromId(), nameById) + "에서 진화");
        }
        if (p.evolvesTo() != null && !p.evolvesTo().isEmpty()) {
            String to = p.evolvesTo().stream()
                    .map(raw -> describeEvolveTo(raw, nameById))
                    .collect(Collectors.joining(", "));
            parts.add(to + "(으)로 진화");
        }
        return String.join(", ", parts);
    }

    private static String describeEvolveTo(String raw, Map<Integer, String> nameById) {
        String[] split = raw.split(":", 2);
        int id = Integer.parseInt(split[0]);
        String condition = split.length > 1 ? split[1] : "";
        String name = nameOf(id, nameById);
        return condition.isEmpty() ? name : name + "(" + condition + ")";
    }

    private static String nameOf(int id, Map<Integer, String> nameById) {
        return nameById.getOrDefault(id, id + "번");
    }

    static String townTitle(TownJson t) {
        return t.nameKo();
    }

    static String townContent(TownJson t) {
        return "%s(%s)\n설명: %s\n주요 장소: %s".formatted(
                t.nameKo(), t.nameEn(), t.description(), String.join(", ", t.notablePlaces()));
    }

    static String gymTitle(GymJson g) {
        return g.townNameKo() + " 체육관";
    }

    static String gymContent(GymJson g) {
        return "체육관장: %s\n타입: %s\n배지: %s\n설명: %s".formatted(
                g.leaderKo(), g.type(), g.badgeKo(), g.description());
    }

    record PokemonJson(int id, String nameKo, String genusKo, List<String> types, StatsJson stats,
                        String spriteUrl, String flavorTextKo, Integer evolvesFromId, List<String> evolvesTo,
                        List<String> abilities, String habitatKo, String colorKo) {
    }

    record StatsJson(int hp, int attack, int defense, int specialAttack, int specialDefense, int speed) {
    }

    record TownJson(int id, String nameKo, String nameEn, String description, List<String> notablePlaces) {
    }

    record GymJson(int id, int order, int townId, String townNameKo, String leaderKo, String type, String badgeKo,
                    String description) {
    }
}
