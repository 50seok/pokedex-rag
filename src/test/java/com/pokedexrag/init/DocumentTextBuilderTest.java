package com.pokedexrag.init;

import com.pokedexrag.init.DocumentTextBuilder.GymJson;
import com.pokedexrag.init.DocumentTextBuilder.PokemonJson;
import com.pokedexrag.init.DocumentTextBuilder.StatsJson;
import com.pokedexrag.init.DocumentTextBuilder.TownJson;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * data/pokemon.json id=1(이상해씨) 등 고정 샘플로 title/content 조립을 검증한다.
 */
class DocumentTextBuilderTest {

    @Test
    void pokemonTitleAndContent_matchBulbasaurSample() {
        PokemonJson bulbasaur = new PokemonJson(
                1, "이상해씨", "씨앗포켓몬", List.of("grass", "poison"),
                new StatsJson(45, 49, 49, 65, 65, 45),
                "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png",
                "태어났을 때부터 등에 이상한 씨앗이 심어져 있으며 몸과 함께 자란다고 한다.");

        assertThat(DocumentTextBuilder.pokemonTitle(bulbasaur)).isEqualTo("이상해씨 (씨앗포켓몬)");
        assertThat(DocumentTextBuilder.pokemonContent(bulbasaur)).isEqualTo(
                "이름: 이상해씨\n분류: 씨앗포켓몬\n타입: grass, poison\n"
                        + "종족값: HP 45 공격 49 방어 49 특공 65 특방 65 스피드 45\n"
                        + "설명: 태어났을 때부터 등에 이상한 씨앗이 심어져 있으며 몸과 함께 자란다고 한다.");
    }

    @Test
    void townTitleAndContent_matchPalletTownSample() {
        TownJson palletTown = new TownJson(
                1, "태초마을", "Pallet Town",
                "관동 지방 남서쪽 끝에 자리한 작은 마을. 오박사 연구소가 있어 트레이너들이 처음 포켓몬을 받고 여행을 시작하는 곳이다.",
                List.of("오박사 연구소", "주인공의 집", "라이벌의 집"));

        assertThat(DocumentTextBuilder.townTitle(palletTown)).isEqualTo("태초마을");
        assertThat(DocumentTextBuilder.townContent(palletTown)).isEqualTo(
                "태초마을(Pallet Town)\n"
                        + "설명: 관동 지방 남서쪽 끝에 자리한 작은 마을. 오박사 연구소가 있어 트레이너들이 처음 포켓몬을 받고 여행을 시작하는 곳이다.\n"
                        + "주요 장소: 오박사 연구소, 주인공의 집, 라이벌의 집");
    }

    @Test
    void gymTitleAndContent_matchPewterGymSample() {
        GymJson pewterGym = new GymJson(
                1, 1, 3, "회색시티", "웅", "rock", "회색배지",
                "관동 최초의 체육관. 바위 타입을 주로 다루며, 방어력이 높은 포켓몬 상대로 첫 배지를 노리는 초보 트레이너들의 관문이다.");

        assertThat(DocumentTextBuilder.gymTitle(pewterGym)).isEqualTo("회색시티 체육관");
        assertThat(DocumentTextBuilder.gymContent(pewterGym)).isEqualTo(
                "체육관장: 웅\n타입: rock\n배지: 회색배지\n"
                        + "설명: 관동 최초의 체육관. 바위 타입을 주로 다루며, 방어력이 높은 포켓몬 상대로 첫 배지를 노리는 초보 트레이너들의 관문이다.");
    }
}
