package com.pokedexrag.util;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 포켓몬 타입별 약점(2배 데미지를 받는 상대 타입) 고정 상성표.
 * Thymeleaf에서 {@code ${T(com.pokedexrag.util.TypeEffectiveness).weaknessesOf(pokemon.types)}}로 호출한다.
 * // ponytail: 단순 약점 합집합, 저항·무효 포함한 정확한 배율 계산은 안 함
 */
public final class TypeEffectiveness {

    private static final Map<String, List<String>> WEAKNESSES = Map.ofEntries(
            Map.entry("normal", List.of("fighting")),
            Map.entry("fire", List.of("water", "ground", "rock")),
            Map.entry("water", List.of("electric", "grass")),
            Map.entry("electric", List.of("ground")),
            Map.entry("grass", List.of("fire", "ice", "poison", "flying", "bug")),
            Map.entry("ice", List.of("fire", "fighting", "rock", "steel")),
            Map.entry("fighting", List.of("flying", "psychic", "fairy")),
            Map.entry("poison", List.of("ground", "psychic")),
            Map.entry("ground", List.of("water", "grass", "ice")),
            Map.entry("flying", List.of("electric", "ice", "rock")),
            Map.entry("psychic", List.of("bug", "ghost", "dark")),
            Map.entry("bug", List.of("fire", "flying", "rock")),
            Map.entry("rock", List.of("water", "grass", "fighting", "ground", "steel")),
            Map.entry("ghost", List.of("ghost", "dark")),
            Map.entry("dragon", List.of("ice", "dragon", "fairy")),
            Map.entry("dark", List.of("fighting", "bug", "fairy")),
            Map.entry("steel", List.of("fire", "fighting", "ground")),
            Map.entry("fairy", List.of("poison", "steel"))
    );

    private TypeEffectiveness() {
    }

    public static List<String> weaknessesOf(List<String> types) {
        Set<String> result = new LinkedHashSet<>();
        for (String type : types) {
            result.addAll(WEAKNESSES.getOrDefault(type, List.of()));
        }
        return List.copyOf(result);
    }
}
