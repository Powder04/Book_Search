package com.example.Book_Search.model;

import java.io.Serializable;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest implements Serializable {
    private String searchId;
    private String keyword;
    private String keywordTrans;
}
