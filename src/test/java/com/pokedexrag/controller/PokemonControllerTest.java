package com.pokedexrag.controller;

import com.pokedexrag.entity.Pokemon;
import com.pokedexrag.service.PokedexService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PokemonController.class)
class PokemonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PokedexService pokedexService;

    @Test
    void list_returns200WithPokemons() throws Exception {
        given(pokedexService.findAllPokemon()).willReturn(List.of(pikachu()));

        mockMvc.perform(get("/pokemon"))
                .andExpect(status().isOk())
                .andExpect(view().name("pokemon/list"))
                .andExpect(model().attributeExists("pokemons"));
    }

    @Test
    void detail_returns200WithPokemon() throws Exception {
        Pokemon pikachu = pikachu();
        given(pokedexService.findPokemon(25)).willReturn(pikachu);

        mockMvc.perform(get("/pokemon/25"))
                .andExpect(status().isOk())
                .andExpect(view().name("pokemon/detail"))
                .andExpect(model().attribute("pokemon", pikachu));
    }

    @Test
    void detail_returns404WhenNotFoundAndNotSwallowedByGlobalHandler() throws Exception {
        given(pokedexService.findPokemon(9999))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "포켓몬을 찾을 수 없습니다"));

        mockMvc.perform(get("/pokemon/9999"))
                .andExpect(status().isNotFound());
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
}
