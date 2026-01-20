package com.example.SeaTea.domain.place.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SpaceCursor {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Long lastId;
    private final String sort;
    private final Double lastDistance;

    // 커서 토큰 인코딩
    public static String encode(SpaceCursor cursor) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("lastId", cursor.lastId);
            payload.put("sort", cursor.sort);
            payload.put("lastDistance", cursor.lastDistance);
            String json = OBJECT_MAPPER.writeValueAsString(payload);
            return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to encode cursor", e);
        }
    }

    // 커서 역직렬화
    public static SpaceCursor decode(String token) {
        try {
            String json = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            Map<String, Object> payload = OBJECT_MAPPER.readValue(json,
                new TypeReference<Map<String, Object>>() {});
            Long lastId = payload.get("lastId") == null ? null : ((Number) payload.get("lastId")).longValue();
            String sort = payload.get("sort") == null ? null : payload.get("sort").toString();
            Double lastDistance = payload.get("lastDistance") == null ? null
                : ((Number) payload.get("lastDistance")).doubleValue();
            return new SpaceCursor(lastId, sort, lastDistance);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid cursor", e);
        }
    }
}
