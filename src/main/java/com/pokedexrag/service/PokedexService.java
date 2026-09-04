package com.pokedexrag.service;

import com.pokedexrag.entity.Gym;
import com.pokedexrag.entity.Pokemon;
import com.pokedexrag.entity.Town;
import com.pokedexrag.repository.GymRepository;
import com.pokedexrag.repository.PokemonRepository;
import com.pokedexrag.repository.TownRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PokedexService {

    private final PokemonRepository pokemonRepository;
    private final TownRepository townRepository;
    private final GymRepository gymRepository;

    public PokedexService(PokemonRepository pokemonRepository, TownRepository townRepository,
                           GymRepository gymRepository) {
        this.pokemonRepository = pokemonRepository;
        this.townRepository = townRepository;
        this.gymRepository = gymRepository;
    }

    public List<Pokemon> findAllPokemon() {
        return pokemonRepository.findAll();
    }

    public Pokemon findPokemon(int id) {
        return pokemonRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "포켓몬을 찾을 수 없습니다"));
    }

    public List<Town> findAllTowns() {
        return townRepository.findAll();
    }

    public Town findTown(int id) {
        return townRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "마을을 찾을 수 없습니다"));
    }

    public List<Gym> findAllGyms() {
        return gymRepository.findAll();
    }

    public GymDetail findGymDetail(int id) {
        Gym gym = gymRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "체육관을 찾을 수 없습니다"));
        Town town = townRepository.findById(gym.getTownId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "마을을 찾을 수 없습니다"));
        return new GymDetail(gym, town);
    }

    public record GymDetail(Gym gym, Town town) {
    }
}
