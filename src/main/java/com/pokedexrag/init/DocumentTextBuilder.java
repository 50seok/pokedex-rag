package com.pokedexrag.init;

import java.util.List;

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

    static String pokemonContent(PokemonJson p) {
        return "이름: %s\n분류: %s\n타입: %s\n종족값: HP %d 공격 %d 방어 %d 특공 %d 특방 %d 스피드 %d\n설명: %s".formatted(
                p.nameKo(), p.genusKo(), String.join(", ", p.types()),
                p.stats().hp(), p.stats().attack(), p.stats().defense(),
                p.stats().specialAttack(), p.stats().specialDefense(), p.stats().speed(),
                p.flavorTextKo());
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
                        String spriteUrl, String flavorTextKo) {
    }

    record StatsJson(int hp, int attack, int defense, int specialAttack, int specialDefense, int speed) {
    }

    record TownJson(int id, String nameKo, String nameEn, String description, List<String> notablePlaces) {
    }

    record GymJson(int id, int order, int townId, String townNameKo, String leaderKo, String type, String badgeKo,
                    String description) {
    }
}
