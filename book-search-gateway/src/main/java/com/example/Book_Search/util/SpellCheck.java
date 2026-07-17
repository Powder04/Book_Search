package com.example.Book_Search.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import lombok.RequiredArgsConstructor;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SpellCheck {
    private final RestTemplate restTemplate;
    @Value("${app.api.spellcheck.url}")
    private String apiURL;
    @Value("${app.api.spellcheck.key}")
    private String apiKey;

    public String fixKeyword(String keyword) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("model", "gpt-5.4-mini");

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> msg = new HashMap<>();

            msg.put("role", "user");
            msg.put("content",
                "You are a spelling correction tool.\n" +
                "Detect the language of the keyword and correct spelling accordingly.\n" +
                "- If Vietnamese: return proper Vietnamese with full diacritics.\n" +
                "- If English: return correct English spelling.\n" +
                "- If mixed: correct both parts appropriately.\n" +
                "Fix ALL spelling mistakes.\n" +
                "Return ONLY the corrected keyword.\n\n" +

                "Examples:\n" +
                "mangj may tinh -> mạng máy tính\n" +
                "smatphone gaming -> smartphone gaming\n" +
                "dien thoai smatphone -> điện thoại smartphone\n\n" +

                "Keyword: " + keyword);

            messages.add(msg);
            body.put("messages", messages);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    apiURL,
                    HttpMethod.POST,
                    request,
                    Map.class);

            List choices = (List) response.getBody().get("choices");
            Map choice = (Map) choices.get(0);
            Map message = (Map) choice.get("message");
            System.err.println(message.get("content").toString().trim());
            return message.get("content").toString().trim();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return keyword;
    }
}
