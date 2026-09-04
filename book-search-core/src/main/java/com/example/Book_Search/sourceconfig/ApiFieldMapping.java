package com.example.Book_Search.sourceconfig;

import lombok.Data;

@Data
public class ApiFieldMapping {
    public enum Mode { VALUE, JOIN, TEMPLATE }
    private String path;
    private Mode mode = Mode.VALUE;
    private String separator = ", ";
    private String subPath;
    private String template;
}
