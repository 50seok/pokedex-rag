package com.pokedexrag.controller;

import com.pokedexrag.entity.Gym;
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

@WebMvcTest(GymController.class)
class GymControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PokedexService pokedexService;

    @Test
    void list_returns200WithGyms() throws Exception {
        given(pokedexService.findAllGyms()).willReturn(List.of(brock()));

        mockMvc.perform(get("/gym"))
                .andExpect(status().isOk())
                .andExpect(view().name("gym/list"))
                .andExpect(model().attributeExists("gyms"));
    }

    @Test
    void detail_returns200WithGymAndTown() throws Exception {
        Gym brock = brock();
        Town pallet = pallet();
        given(pokedexService.findGymDetail(1)).willReturn(new PokedexService.GymDetail(brock, pallet));

        mockMvc.perform(get("/gym/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("gym/detail"))
                .andExpect(model().attribute("gym", brock))
                .andExpect(model().attribute("town", pallet));
    }

    @Test
    void detail_returns404WhenNotFoundAndNotSwallowedByGlobalHandler() throws Exception {
        given(pokedexService.findGymDetail(9999))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "체육관을 찾을 수 없습니다"));

        mockMvc.perform(get("/gym/9999"))
                .andExpect(status().isNotFound());
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
