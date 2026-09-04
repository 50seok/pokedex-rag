package com.pokedexrag.controller;

import com.pokedexrag.service.PokedexService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class TownController {

    private final PokedexService pokedexService;

    public TownController(PokedexService pokedexService) {
        this.pokedexService = pokedexService;
    }

    @GetMapping("/town")
    public String list(Model model) {
        model.addAttribute("towns", pokedexService.findAllTowns());
        return "town/list";
    }

    @GetMapping("/town/{id}")
    public String detail(@PathVariable int id, Model model) {
        model.addAttribute("town", pokedexService.findTown(id));
        return "town/detail";
    }
}
