package com.pokedexrag.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "pokemon")
public class Pokemon {

    @Id
    private Integer id;

    @Column(name = "name_ko", nullable = false)
    private String nameKo;

    @Column(name = "genus_ko", nullable = false)
    private String genusKo;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(nullable = false)
    private List<String> types;

    @Column(nullable = false)
    private Integer hp;

    @Column(nullable = false)
    private Integer attack;

    @Column(nullable = false)
    private Integer defense;

    @Column(name = "special_attack", nullable = false)
    private Integer specialAttack;

    @Column(name = "special_defense", nullable = false)
    private Integer specialDefense;

    @Column(nullable = false)
    private Integer speed;

    @Column(name = "sprite_url")
    private String spriteUrl;

    @Column(name = "flavor_text_ko", nullable = false)
    private String flavorTextKo;

    @Builder
    public Pokemon(Integer id, String nameKo, String genusKo, List<String> types, Integer hp, Integer attack,
                   Integer defense, Integer specialAttack, Integer specialDefense, Integer speed,
                   String spriteUrl, String flavorTextKo) {
        this.id = id;
        this.nameKo = nameKo;
        this.genusKo = genusKo;
        this.types = types;
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
        this.specialAttack = specialAttack;
        this.specialDefense = specialDefense;
        this.speed = speed;
        this.spriteUrl = spriteUrl;
        this.flavorTextKo = flavorTextKo;
    }
}
