package com.pokedexrag.repository;

import com.pokedexrag.entity.Town;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TownRepository extends JpaRepository<Town, Integer> {
}
