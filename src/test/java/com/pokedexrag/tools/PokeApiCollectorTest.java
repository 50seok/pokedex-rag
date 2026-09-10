package com.pokedexrag.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * evolutionConditionText()는 PokeAPI evolution-chain 응답의 evolution_details 배열을 한국어 문구로 바꾼다.
 * 패키지 접근 static 메서드라 별도 인스턴스 없이 직접 호출한다.
 */
class PokeApiCollectorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void evolutionConditionText_levelUpWithMinLevel_returnsLevelText() throws Exception {
        JsonNode details = parse("""
                [ { "trigger": { "name": "level-up" }, "min_level": 16 } ]
                """);

        assertThat(PokeApiCollector.evolutionConditionText(details)).isEqualTo("16레벨");
    }

    @Test
    void evolutionConditionText_levelUpWithMinHappiness_returnsHappinessText() throws Exception {
        JsonNode details = parse("""
                [ { "trigger": { "name": "level-up" }, "min_happiness": 220 } ]
                """);

        assertThat(PokeApiCollector.evolutionConditionText(details)).isEqualTo("친밀도 220 이상");
    }

    @Test
    void evolutionConditionText_useItem_returnsItemLabelWithSuffix() throws Exception {
        JsonNode details = parse("""
                [ { "trigger": { "name": "use-item" }, "item": { "name": "water-stone" } } ]
                """);

        assertThat(PokeApiCollector.evolutionConditionText(details)).isEqualTo("물의돌 사용");
    }

    @Test
    void evolutionConditionText_trade_returnsTradeText() throws Exception {
        JsonNode details = parse("""
                [ { "trigger": { "name": "trade" } } ]
                """);

        assertThat(PokeApiCollector.evolutionConditionText(details)).isEqualTo("교환");
    }

    @Test
    void evolutionConditionText_unknownTrigger_returnsTriggerNameAsIs() throws Exception {
        JsonNode details = parse("""
                [ { "trigger": { "name": "shed" } } ]
                """);

        assertThat(PokeApiCollector.evolutionConditionText(details)).isEqualTo("shed");
    }

    @Test
    void evolutionConditionText_emptyDetails_returnsEmptyString() throws Exception {
        JsonNode details = parse("[]");

        assertThat(PokeApiCollector.evolutionConditionText(details)).isEmpty();
    }

    private JsonNode parse(String json) throws Exception {
        return objectMapper.readTree(json);
    }
}
