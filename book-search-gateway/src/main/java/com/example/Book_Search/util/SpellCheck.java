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
                "Bạn là công cụ sửa lỗi chính tả cho keyword.\n" + 
                "Nhiệm vụ:\n" +
                "- Nhận vào một keyword do người dùng gửi.\n" +
                "- Sửa các lỗi chính tả, lỗi gõ sai và dấu câu nếu cần.\n" +
                "- Giữ nguyên ý nghĩa, cấu trúc và ngôn ngữ của keyword.\n" +
                "- Không thêm, bớt hoặc diễn giải nội dung nếu không cần thiết.\n" +
                "- Nếu keyword đã đúng chính tả, giữ nguyên.\n" +
                "- Chỉ trả về keyword sau khi đã sửa.\n" +
                "- Không giải thích, không thêm dấu ngoặc kép, không thêm bất kỳ nội dung nào khác.\n" +
                
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
