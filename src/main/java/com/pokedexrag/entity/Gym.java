package com.pokedexrag.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "gym")
public class Gym {

    @Id
    private Integer id;

    @Column(name = "town_id", nullable = false)
    private Integer townId;

    @Column(name = "gym_order", nullable = false)
    private Integer order;

    @Column(name = "leader_ko", nullable = false)
    private String leaderKo;

    @Column(nullable = false)
    private String type;

    @Column(name = "badge_ko", nullable = false)
    private String badgeKo;

    @Column(nullable = false)
    private String description;

    @Builder
    public Gym(Integer id, Integer townId, Integer order, String leaderKo, String type, String badgeKo,
               String description) {
        this.id = id;
        this.townId = townId;
        this.order = order;
        this.leaderKo = leaderKo;
        this.type = type;
        this.badgeKo = badgeKo;
        this.description = description;
    }
}
