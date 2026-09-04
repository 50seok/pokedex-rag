package com.pokedexrag.controller;

import com.pokedexrag.service.PokedexService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class GymController {

    private final PokedexService pokedexService;

    public GymController(PokedexService pokedexService) {
        this.pokedexService = pokedexService;
    }

    @GetMapping("/gym")
    public String list(Model model) {
        model.addAttribute("gyms", pokedexService.findAllGyms());
        return "gym/list";
    }

    @GetMapping("/gym/{id}")
    public String detail(@PathVariable int id, Model model) {
        PokedexService.GymDetail detail = pokedexService.findGymDetail(id);
        model.addAttribute("gym", detail.gym());
        model.addAttribute("town", detail.town());
        return "gym/detail";
    }
}
