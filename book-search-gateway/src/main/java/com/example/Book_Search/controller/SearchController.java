package com.example.Book_Search.controller;

import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.Book_Search.model.SearchRequest;
import com.example.Book_Search.service.SearchProducerService;
import com.example.Book_Search.util.SpellCheck;
import com.example.Book_Search.util.Translate;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SearchController {
    private final SearchProducerService service;
    private final Translate translate;
    private final SpellCheck spellCheck;

    @PostMapping("/search")
    public Map<String, String> searchBook(@RequestParam String keyword) {
        String searchID = UUID.randomUUID().toString();
        System.out.println(searchID);
        String keywordFixed = spellCheck.fixKeyword(keyword);
        // String keyword1 = keyword; 
        String keywordTrans = translate.translate(keywordFixed);

        SearchRequest request = new SearchRequest(
                        searchID,
                        keywordFixed,
                        keywordTrans);

        service.publishSearchRequest(request);

        return Map.of(
            "searchId", searchID,
            "keyword", keywordFixed,
            "keywordTrans", keywordTrans
        ); // Tra ID ve cho C => Lay ID bao danh de nhan kq
    }
}
