package com.example.Book_Search.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "app.rabbitmq")
public class RabbitMQProperties {
    private String directExchange;
    private String topicExchange;
    private String fanoutExchange;
    private Request request = new Request();
    private Result result = new Result();

    @Data
    public static class Request {
        private String queue;
        private String routing;
    }

    @Data
    public static class Result {
        private String queue;
        private String routing;
    }
}
