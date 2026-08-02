package com.example.Book_Search.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.*;

@Configuration
@RequiredArgsConstructor
public class RabbitMQConfig {
    private final RabbitMQProperties properties;
    private final AmqpAdmin rabbitAdmin;

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
        rabbitAdmin.declareExchange(new TopicExchange(properties.getTopicExchange()));

        Queue requestQueue = new Queue(properties.getRequest().getQueue());
        rabbitAdmin.declareQueue(requestQueue);
        rabbitAdmin.declareBinding(BindingBuilder
                .bind(requestQueue)
                .to(directExchange)
                .with(properties.getRequest().getRouting()));
    }
}
