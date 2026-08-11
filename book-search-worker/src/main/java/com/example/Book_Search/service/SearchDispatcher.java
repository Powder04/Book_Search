package com.example.Book_Search.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import com.example.Book_Search.config.RabbitMQProperties;
import com.example.Book_Search.model.*;
import com.example.Book_Search.util.SourceRegistry;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SearchDispatcher {
    private final RabbitTemplate rabbitTemplate;
    private final SourceRegistry registry;
    private final RabbitMQProperties rabbitmqProperties;

    @RabbitListener(queues = "#{rabbitMQProperties.request.queue}",
                    containerFactory = "dispatcherFactory")
    public void dispatch(SearchRequest request) {
        for(OpenResource resource : registry.getAllSources()) {
            rabbitTemplate.convertAndSend(
                    rabbitmqProperties.getTopicExchange(),
                    resource.getRoutingKey(),
                    request
            );
        }
    }
}
