package com.dinhluong.dlmstore.utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GeminiClient {

    RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Value("${gemini.api.key}")
    private String apiKey;

    public String callGemini(String promptText) {
        try {
            Map<String, Object> contentPart = new HashMap<>();
            contentPart.put("text", promptText);

            Map<String, Object> content = new HashMap<>();
            content.put("parts", List.of(contentPart));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(content));

            // Call API
            String finalUrl = apiUrl + "?key=" + apiKey;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            String response = restTemplate.postForObject(finalUrl, entity, String.class);

            // Parse Response
            JsonNode rootNode = objectMapper.readTree(response);
            if (rootNode.path("candidates").isEmpty()) {
                return "Xin lỗi, AI không phản hồi (Safety block).";
            }
            return rootNode.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

        } catch (Exception e) {
            e.printStackTrace();
            return "Hệ thống đang bận, vui lòng thử lại sau.";
        }
    }
}