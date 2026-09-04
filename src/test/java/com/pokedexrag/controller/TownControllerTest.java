package com.pokedexrag.controller;

import com.pokedexrag.entity.Town;
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

@WebMvcTest(TownController.class)
class TownControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PokedexService pokedexService;

    @Test
    void list_returns200WithTowns() throws Exception {
        given(pokedexService.findAllTowns()).willReturn(List.of(pallet()));

        mockMvc.perform(get("/town"))
                .andExpect(status().isOk())
                .andExpect(view().name("town/list"))
                .andExpect(model().attributeExists("towns"));
    }

    @Test
    void detail_returns200WithTown() throws Exception {
        Town pallet = pallet();
        given(pokedexService.findTown(1)).willReturn(pallet);

        mockMvc.perform(get("/town/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("town/detail"))
                .andExpect(model().attribute("town", pallet));
    }

    @Test
    void detail_returns404WhenNotFoundAndNotSwallowedByGlobalHandler() throws Exception {
        given(pokedexService.findTown(9999))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "마을을 찾을 수 없습니다"));

        mockMvc.perform(get("/town/9999"))
                .andExpect(status().isNotFound());
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
}
