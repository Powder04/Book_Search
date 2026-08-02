package com.example.Book_Search.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "app.general")
public class GeneralProperties {
    private Dispatcher dispatcher = new Dispatcher();
    private Worker worker = new Worker();
    private Result result = new Result();

    @Data
    public static class Dispatcher {
        private int consume;
        private int maxConsume;
        private int prefetchCount;
    }

    @Data
    public static class Worker {
        private int consume;
        private int maxConsume;
        private int prefetchCount;
    }

    @Data
    public static class Result {
        private int consume;
        private int maxConsume;
        private int prefetchCount; 
    }
}
