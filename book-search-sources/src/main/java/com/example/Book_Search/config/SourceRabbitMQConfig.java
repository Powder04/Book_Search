package com.example.Book_Search.config;

import com.example.Book_Search.model.OpenResource;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SourceRabbitMQConfig {

    private final RabbitMQProperties properties;
    private final AmqpAdmin rabbitAdmin;
    private final List<OpenResource> resources;

    @PostConstruct
    public void init() {
        
        TopicExchange topicExchange = new TopicExchange(properties.getTopicExchange());
        rabbitAdmin.declareExchange(topicExchange);

        for (OpenResource resource : resources) {
            Queue queue = new Queue(resource.getQueueName());
            rabbitAdmin.declareQueue(queue);
            rabbitAdmin.declareBinding(BindingBuilder
                    .bind(queue)
                    .to(topicExchange)
                    .with(resource.getRoutingKey()));
        }
    }
}
