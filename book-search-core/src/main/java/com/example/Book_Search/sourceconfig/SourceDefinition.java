package com.example.Book_Search.sourceconfig;

import lombok.Data;

@Data
public abstract class SourceDefinition {
    private String name;
    private String routingKey;
    private String queueName;
}
