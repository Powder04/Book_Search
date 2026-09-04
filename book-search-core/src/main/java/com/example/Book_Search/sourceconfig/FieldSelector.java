package com.example.Book_Search.sourceconfig;

import lombok.Data;

@Data
public class FieldSelector {
    public enum ExtractType { TEXT, ATTR }
    private String selector;
    private ExtractType extract = ExtractType.TEXT;
    private String attr;
    private String urlPrefix = "";
}
