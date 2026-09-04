package com.example.Book_Search.model;

import java.io.Serializable;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult implements Serializable {
    private String searchId;
    private String source;
    private String title;
    private String author;
    private String publishYear;
    private String language;
    private String coverUrl;
    private String isbn; 
    private String url;
    private String type;
}
