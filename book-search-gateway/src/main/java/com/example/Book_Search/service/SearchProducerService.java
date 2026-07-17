package com.example.Book_Search.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import com.example.Book_Search.config.RabbitMQProperties;
import com.example.Book_Search.model.SearchRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchProducerService {
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQProperties rabbitmqProperties;

    public void publishSearchRequest(SearchRequest request) {
        rabbitTemplate.convertAndSend(
                rabbitmqProperties.getDirectExchange(),
                rabbitmqProperties.getRequest().getRouting(),
                request
        );
    }
}
