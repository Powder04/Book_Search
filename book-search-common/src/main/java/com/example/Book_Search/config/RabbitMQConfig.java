package com.example.Book_Search.config;

import com.example.Book_Search.model.OpenResource;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.*;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class RabbitMQConfig {
    private final RabbitMQProperties properties;
    private final AmqpAdmin rabbitAdmin;
    private final List<OpenResource> resources;

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(properties.getDirectExchange());
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(properties.getTopicExchange());
    }

    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange(properties.getFanoutExchange());
    }
    
    @PostConstruct
    public void init() {
        DirectExchange directExchange = new DirectExchange(properties.getDirectExchange());
        rabbitAdmin.declareExchange(directExchange);
        rabbitAdmin.declareExchange(new FanoutExchange(properties.getFanoutExchange()));

        Queue requestQueue = new Queue(properties.getRequest().getQueue());
        rabbitAdmin.declareQueue(requestQueue);
        rabbitAdmin.declareBinding(BindingBuilder
                .bind(requestQueue)
                .to(directExchange)
                .with(properties.getRequest().getRouting()));

        // Declare TopicExchange + bindings cho worker queues
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
