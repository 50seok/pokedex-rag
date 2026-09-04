package com.pokedexrag.controller;

import com.pokedexrag.service.PokedexService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PokemonController {

    private final PokedexService pokedexService;

    public PokemonController(PokedexService pokedexService) {
        this.pokedexService = pokedexService;
    }

    @GetMapping("/pokemon")
    public String list(Model model) {
        model.addAttribute("pokemons", pokedexService.findAllPokemon());
        return "pokemon/list";
    }

    @GetMapping("/pokemon/{id}")
    public String detail(@PathVariable int id, Model model) {
        model.addAttribute("pokemon", pokedexService.findPokemon(id));
        return "pokemon/detail";
    }
}
