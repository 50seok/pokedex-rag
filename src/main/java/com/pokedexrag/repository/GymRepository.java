package com.pokedexrag.repository;

import com.pokedexrag.entity.Gym;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GymRepository extends JpaRepository<Gym, Integer> {
}
