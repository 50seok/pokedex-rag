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
@Table(name = "town")
public class Town {

    @Id
    private Integer id;

    @Column(name = "name_ko", nullable = false)
    private String nameKo;

    @Column(name = "name_en", nullable = false)
    private String nameEn;

    @Column(nullable = false)
    private String description;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "notable_places", nullable = false)
    private List<String> notablePlaces;

    @Builder
    public Town(Integer id, String nameKo, String nameEn, String description, List<String> notablePlaces) {
        this.id = id;
        this.nameKo = nameKo;
        this.nameEn = nameEn;
        this.description = description;
        this.notablePlaces = notablePlaces;
    }
}
