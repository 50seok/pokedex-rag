package com.pokedexrag.repository;

import com.pokedexrag.dto.DocumentSearchResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * pgvector {@code vector} 타입은 Spring Data JPA가 다루지 못해 JdbcTemplate으로 직접 다룬다.
 */
@Repository
public class DocumentRepository {

    private final JdbcTemplate jdbcTemplate;

    public DocumentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void deleteAll() {
        jdbcTemplate.update("DELETE FROM document");
    }

    public void insert(String sourceType, int sourceId, String title, String content, float[] embedding) {
        jdbcTemplate.update(
                "INSERT INTO document (source_type, source_id, title, content, embedding) VALUES (?, ?, ?, ?, ?::vector)",
                sourceType, sourceId, title, content, toVectorLiteral(embedding));
    }

    public List<DocumentSearchResult> searchTopK(float[] queryEmbedding, int k) {
        return jdbcTemplate.query(
                "SELECT id, source_type, source_id, title, content, embedding <=> ?::vector AS distance "
                        + "FROM document ORDER BY distance ASC LIMIT ?",
                (rs, rowNum) -> new DocumentSearchResult(
                        rs.getLong("id"),
                        rs.getString("source_type"),
                        rs.getInt("source_id"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getDouble("distance")),
                toVectorLiteral(queryEmbedding), k);
    }

    private String toVectorLiteral(float[] embedding) {
        return IntStream.range(0, embedding.length)
                .mapToObj(i -> Float.toString(embedding[i]))
                .collect(Collectors.joining(",", "[", "]"));
    }
}
