package com.dinhluong.dlmstore.utils;

import com.dinhluong.dlmstore.config.GeminiProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.web.client.HttpStatusCodeException;
@Component
@RequiredArgsConstructor
public class GeminiClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final GeminiProperties geminiProperties;

    private final AtomicInteger currentIndex = new AtomicInteger(0);

    public String callGemini(String promptText) {
        List<String> apiKeys = geminiProperties.getKeys();
        if (apiKeys == null || apiKeys.isEmpty()) {
            return "Chưa cấu hình Gemini API key.";
        }

        int totalKeys = apiKeys.size();

        for (int attempt = 0; attempt < totalKeys; attempt++) {
            String apiKey = getNextApiKey(apiKeys);
            try {
                String result = callGeminiWithKey(promptText, apiKey);
                if (result != null && !result.isBlank()) {
                    return result;
                }
            }
            // 1. TRƯỜNG HỢP QUÁ TẢI HẠN MỨC (429) -> TIẾP TỤC VÒNG LẶP ĐỂ ĐỔI KEY
            catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests e) {
                System.out.println("⚠️ Key bị giới hạn Quota (429) -> Đang tự động đổi sang key tiếp theo...");
                // Không return, vòng lặp for sẽ chạy tiếp để thử key mới
            }
            // 2. TRƯỜNG HỢP SERVER GOOGLE LỖI (503) -> DỪNG VÀ BÁO LỖI LUÔN
            catch (org.springframework.web.client.HttpServerErrorException.ServiceUnavailable e) {
                System.err.println("❌ Lỗi 503: Server Google đang quá tải thực sự. Dừng tiến trình.");
                return "⚠️ Hiện tại máy chủ AI của Google đang quá tải (Lỗi 503), vui lòng thử lại sau ít phút.";
            }
            // 3. CÁC LỖI HTTP KHÁC (401, 400, 500...) -> BÁO LỖI LUÔN
            catch (org.springframework.web.client.HttpStatusCodeException e) {
                System.err.println("❌ Lỗi HTTP " + e.getStatusCode() + ": " + e.getResponseBodyAsString());
                return "⚠️ Hệ thống gặp lỗi khi gọi AI: " + e.getStatusCode();
            }
            // 4. LỖI KẾT NỐI
            catch (org.springframework.web.client.ResourceAccessException e) {
                return "⚠️ Lỗi kết nối mạng, AI không phản hồi.";
            }
            catch (Exception e) {
                e.printStackTrace();
                return "⚠️ Đã xảy ra lỗi không xác định.";
            }
        }

        return "⚠️ Tất cả các API Key đều đã hết hạn mức sử dụng.";
    }

    private String callGeminiWithKey(String promptText, String apiKey) throws Exception {

        Map<String, Object> contentPart = new HashMap<>();
        contentPart.put("text", promptText);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(contentPart));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(content));

        String finalUrl = geminiProperties.getUrl() + "?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                finalUrl,
                HttpMethod.POST,
                entity,
                String.class
        );

        String response = responseEntity.getBody();

        JsonNode rootNode = objectMapper.readTree(response);

        if (rootNode.path("candidates").isEmpty()) {
            return "AI không phản hồi.";
        }

        return rootNode.path("candidates")
                .get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text")
                .asText();
    }

    private String getNextApiKey(List<String> apiKeys) {

        int index = Math.abs(
                currentIndex.getAndIncrement() % apiKeys.size()
        );

        return apiKeys.get(index);
    }
}