package com.example.Book_Search.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TranslationItem {
    @JsonProperty("detected_source_language")
    private String detected_source_language;
    private String text;
}
