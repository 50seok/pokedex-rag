package com.pokedexrag.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TypeEffectivenessTest {

    @Test
    void weaknessesOf_singleType_returnsItsWeaknesses() {
        assertThat(TypeEffectiveness.weaknessesOf(List.of("grass")))
                .containsExactlyInAnyOrder("fire", "ice", "poison", "flying", "bug");
    }

    @Test
    void weaknessesOf_dualType_returnsUnionWithoutDuplicates() {
        // 이상해씨: grass/poison -> fire, ice, poison, flying, bug, ground, psychic (poison 자기 자신 약점 제외 안 함, 단순 합집합)
        List<String> weaknesses = TypeEffectiveness.weaknessesOf(List.of("grass", "poison"));

        assertThat(weaknesses).containsExactlyInAnyOrder(
                "fire", "ice", "poison", "flying", "bug", "ground", "psychic");
        assertThat(weaknesses).doesNotHaveDuplicates();
    }
}
