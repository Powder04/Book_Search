package com.example.Book_Search.model;

import java.util.List;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TranslateResponse {
    private List<TranslationItem> translations;
}
