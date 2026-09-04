package com.pokedexrag.service;

import com.pokedexrag.entity.Gym;
import com.pokedexrag.entity.Pokemon;
import com.pokedexrag.entity.Town;
import com.pokedexrag.repository.GymRepository;
import com.pokedexrag.repository.PokemonRepository;
import com.pokedexrag.repository.TownRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PokedexServiceTest {

    @Mock
    private PokemonRepository pokemonRepository;
    @Mock
    private TownRepository townRepository;
    @Mock
    private GymRepository gymRepository;

    private PokedexService pokedexService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        pokedexService = new PokedexService(pokemonRepository, townRepository, gymRepository);
    }

    @Test
    void findAllPokemon_returnsAll() {
        given(pokemonRepository.findAll()).willReturn(List.of(pikachu()));

        assertThat(pokedexService.findAllPokemon()).hasSize(1);
    }

    @Test
    void findPokemon_returnsWhenExists() {
        given(pokemonRepository.findById(25)).willReturn(Optional.of(pikachu()));

        assertThat(pokedexService.findPokemon(25).getNameKo()).isEqualTo("피카츄");
    }

    @Test
    void findPokemon_throws404WhenNotFound() {
        given(pokemonRepository.findById(9999)).willReturn(Optional.empty());

        assertThatThrownBy(() -> pokedexService.findPokemon(9999))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void findAllTowns_returnsAll() {
        given(townRepository.findAll()).willReturn(List.of(pallet()));

        assertThat(pokedexService.findAllTowns()).hasSize(1);
    }

    @Test
    void findTown_returnsWhenExists() {
        given(townRepository.findById(1)).willReturn(Optional.of(pallet()));

        assertThat(pokedexService.findTown(1).getNameKo()).isEqualTo("태초마을");
    }

    @Test
    void findTown_throws404WhenNotFound() {
        given(townRepository.findById(9999)).willReturn(Optional.empty());

        assertThatThrownBy(() -> pokedexService.findTown(9999))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void findAllGyms_returnsAll() {
        given(gymRepository.findAll()).willReturn(List.of(brock()));

        assertThat(pokedexService.findAllGyms()).hasSize(1);
    }

    @Test
    void findGymDetail_returnsGymWithTown() {
        given(gymRepository.findById(1)).willReturn(Optional.of(brock()));
        given(townRepository.findById(1)).willReturn(Optional.of(pallet()));

        PokedexService.GymDetail detail = pokedexService.findGymDetail(1);

        assertThat(detail.gym().getLeaderKo()).isEqualTo("웅");
        assertThat(detail.town().getNameKo()).isEqualTo("태초마을");
    }

    @Test
    void findGymDetail_throws404WhenGymNotFound() {
        given(gymRepository.findById(9999)).willReturn(Optional.empty());

        assertThatThrownBy(() -> pokedexService.findGymDetail(9999))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void findGymDetail_throws404WhenTownNotFound() {
        given(gymRepository.findById(1)).willReturn(Optional.of(brock()));
        given(townRepository.findById(1)).willReturn(Optional.empty());

        assertThatThrownBy(() -> pokedexService.findGymDetail(1))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    private Pokemon pikachu() {
        return Pokemon.builder()
                .id(25)
                .nameKo("피카츄")
                .genusKo("쥐 포켓몬")
                .types(List.of("electric"))
                .hp(35).attack(55).defense(40).specialAttack(50).specialDefense(50).speed(90)
                .spriteUrl("https://example.com/25.png")
                .flavorTextKo("전기를 저장한다.")
                .build();
    }

    private Town pallet() {
        return Town.builder()
                .id(1)
                .nameKo("태초마을")
                .nameEn("Pallet Town")
                .description("여행의 시작점")
                .notablePlaces(List.of("오박사 연구소"))
                .build();
    }

    private Gym brock() {
        return Gym.builder()
                .id(1)
                .townId(1)
                .order(1)
                .leaderKo("웅")
                .type("rock")
                .badgeKo("바위뱃지")
                .description("돌 타입 체육관")
                .build();
    }
}
