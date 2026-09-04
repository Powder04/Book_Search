package com.example.Book_Search.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.*;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class ResultFanoutConfig {
    private final RabbitMQProperties properties;

    @Bean
    public Queue gatewayResultQueue() {
        return new AnonymousQueue();
    }

    @Bean
    public Binding gatewayResultBinding(Queue gatewayResultQueue) {
        FanoutExchange resultFanoutExchange = new FanoutExchange(properties.getFanoutExchange());
        return BindingBuilder.bind(gatewayResultQueue).to(resultFanoutExchange);
    }
}