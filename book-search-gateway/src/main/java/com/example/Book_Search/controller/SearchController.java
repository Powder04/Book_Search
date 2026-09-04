package com.example.Book_Search.controller;

import java.util.*;
import org.springframework.web.bind.annotation.*;
import com.example.Book_Search.model.SearchRequest;
import com.example.Book_Search.service.SearchProducerService;
import com.example.Book_Search.util.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SearchController {
    private final SearchProducerService service;
    private final Translate translate;
    private final SpellCheck spellCheck;

    @PostMapping("/search")
    public Map<String, String> searchBook(@RequestParam String keyword, @RequestParam String targetLang) {
        String searchID = UUID.randomUUID().toString();
        System.out.println(searchID);
        String keywordFixed = spellCheck.fixKeyword(keyword);
        String keywordTrans = translate.translate(keywordFixed, targetLang);

        SearchRequest request = new SearchRequest(
                        searchID,
                        keywordFixed,
                        keywordTrans);

        service.publishSearchRequest(request);

        return Map.of(
            "searchId", searchID,
            "keyword", keywordFixed,
            "keywordTrans", keywordTrans
        ); //Tra ID ve cho C => Lay ID bao danh de nhan kq
    }
}
