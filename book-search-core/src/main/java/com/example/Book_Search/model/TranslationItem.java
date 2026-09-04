package com.example.Book_Search.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TranslationItem {
    @JsonProperty("detected_source_language")
    private String detected_source_language;
    private String text;
}
