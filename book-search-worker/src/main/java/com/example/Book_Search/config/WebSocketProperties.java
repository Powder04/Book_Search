package com.example.Book_Search.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "app.websocket")
public class WebSocketProperties {
    private String topicPrefix;
}
