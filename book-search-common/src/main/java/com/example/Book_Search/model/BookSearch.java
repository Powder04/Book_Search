package com.example.Book_Search.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookSearch {
    private String title;
    private String author;
    private String publishYear;
    private String language;
    private String coverUrl;
    private String isbn;
    private String bookUrl;
    private String sourceName;
}
