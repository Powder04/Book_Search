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
                "Bạn là một chuyên gia biên tập ngôn ngữ cao cấp (expert proofreader) thành thạo cả tiếng Việt và tiếng Anh." + 
                "Nhiệm vụ của bạn là rà soát và sửa toàn bộ lỗi chính tả, lỗi đánh máy, lỗi dấu câu và lỗi ngữ pháp trong từ khóa được gửi đến.\n" +
                "Yêu cầu cụ thể:\n" +
                "1. Giữ nguyên hoàn toàn ý nghĩa gốc, văn phong và ngôn ngữ của đoạn văn (nếu là tiếng Việt giữ tiếng Việt, tiếng Anh giữ tiếng Anh).\n" +
                "2. Sửa lại các từ sao cho hợp lí nhất có thể.\n" +
                "3. Kết quả đầu ra sẽ chỉ là cả cụm từ sau khi đã sửa lỗi, không thêm ngoặc kép, không thêm ngoặc đơn, không thêm dấu chấm câu hoặc câu dẫn gì cả.\n" +
                
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
