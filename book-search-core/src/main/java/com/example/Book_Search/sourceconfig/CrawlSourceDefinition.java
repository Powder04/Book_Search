package com.example.Book_Search.sourceconfig;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CrawlSourceDefinition extends SourceDefinition {

    public enum PaginationMode { NONE, SECOND_LAST_TEXT, LAST_TEXT }
    private String listUrlTemplate;
    private String paginationSelector;
    private PaginationMode paginationMode = PaginationMode.NONE;
    private String itemSelector;
    private Map<String, FieldSelector> fields = new LinkedHashMap<>();
    private String staticLanguage = "";
    private String userAgent = "Mozilla/5.0";
    private int timeoutMs = 20000;
    private int strictTitleFilterThreshold = 0;
}
