package com.pokedexrag.util;

import java.util.Map;

/**
 * 포켓몬 타입 영문 slug -> 한글 라벨 매핑.
 * Thymeleaf에서 {@code ${T(com.pokedexrag.util.TypeLabels).labelOf(t)}}로 호출한다.
 */
public final class TypeLabels {

    private static final Map<String, String> LABELS = Map.ofEntries(
            Map.entry("normal", "노말"),
            Map.entry("fire", "불꽃"),
            Map.entry("water", "물"),
            Map.entry("electric", "전기"),
            Map.entry("grass", "풀"),
            Map.entry("ice", "얼음"),
            Map.entry("fighting", "격투"),
            Map.entry("poison", "독"),
            Map.entry("ground", "땅"),
            Map.entry("flying", "비행"),
            Map.entry("psychic", "에스퍼"),
            Map.entry("bug", "벌레"),
            Map.entry("rock", "바위"),
            Map.entry("ghost", "고스트"),
            Map.entry("dragon", "드래곤"),
            Map.entry("dark", "악"),
            Map.entry("steel", "강철"),
            Map.entry("fairy", "페어리")
    );

    private TypeLabels() {
    }

    public static String labelOf(String slug) {
        return LABELS.getOrDefault(slug, slug);
    }
}
