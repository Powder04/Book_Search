package com.example.Book_Search.sourceconfig;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApiSourceDefinition extends SourceDefinition {
    private String baseUrl;
    private String queryTemplate;
    private String itemsPath;
    private Map<String, ApiFieldMapping> fields = new LinkedHashMap<>();
}
