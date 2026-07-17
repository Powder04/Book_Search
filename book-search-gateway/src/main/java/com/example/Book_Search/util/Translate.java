package com.example.Book_Search.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.example.Book_Search.model.TranslateResponse;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
@Service
public class Translate {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    @Value("${app.api.translate.url}")
    private String apiURL;
    @Value("${app.api.translate.key}")
    private String apiKey;

    public String translate(String keyword) {

        Map<String, Object> request = new HashMap<>();
        request.put("text", List.of(keyword));   // DeepL yeu cau text la array
        request.put("source_lang", "VI"); // Ngon ngu goc
        request.put("target_lang", "EN"); // Ngon ngu dich

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "DeepL-Auth-Key " + apiKey);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            TranslateResponse response =
                    restTemplate.postForObject(apiURL, entity, TranslateResponse.class);

            System.out.println("Request: " + objectMapper.writeValueAsString(request));
            System.out.println("Response: " + objectMapper.writeValueAsString(response));

            if (response != null
                    && response.getTranslations() != null
                    && !response.getTranslations().isEmpty()
                    && response.getTranslations().get(0).getText() != null
                    && !response.getTranslations().get(0).getText().isBlank()) {
                return response.getTranslations().get(0).getText();
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return keyword;
    }
}
