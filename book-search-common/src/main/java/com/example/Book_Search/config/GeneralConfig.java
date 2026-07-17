package com.example.Book_Search.config;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GeneralConfig {
    private final GeneralProperties generalProperties;

    @Bean
    public SimpleRabbitListenerContainerFactory dispatcherFactory(ConnectionFactory cf) {
        var f = new SimpleRabbitListenerContainerFactory();
        f.setConnectionFactory(cf);
        f.setMessageConverter(messageConverter());
        f.setConcurrentConsumers(generalProperties.getDispatcher().getConsume());
        f.setMaxConcurrentConsumers(generalProperties.getDispatcher().getMaxConsume());
        f.setPrefetchCount(generalProperties.getDispatcher().getPrefetchCount());
        return f;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory workerFactory(ConnectionFactory cf) {
        var f = new SimpleRabbitListenerContainerFactory();
        f.setConnectionFactory(cf);
        f.setMessageConverter(messageConverter());
        f.setConcurrentConsumers(generalProperties.getWorker().getConsume());
        f.setMaxConcurrentConsumers(generalProperties.getWorker().getMaxConsume());
        f.setPrefetchCount(generalProperties.getWorker().getPrefetchCount());
        return f;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory resultFactory(ConnectionFactory cf) {
        var f = new SimpleRabbitListenerContainerFactory();
        f.setConnectionFactory(cf);
        f.setMessageConverter(messageConverter());
        f.setConcurrentConsumers(generalProperties.getResult().getConsume());
        f.setMaxConcurrentConsumers(generalProperties.getResult().getMaxConsume());
        f.setPrefetchCount(generalProperties.getResult().getPrefetchCount());
        return f;
    }

    // Tao resttemplate, spring boot khong tu tao duoc
    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory f = new HttpComponentsClientHttpRequestFactory();
        f.setConnectionRequestTimeout(8000);
        f.setReadTimeout(15000);
        return new RestTemplate(f);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        RabbitTemplate t = new RabbitTemplate(cf);
        t.setMessageConverter(messageConverter());
        t.setMandatory(true);
        t.setReturnsCallback(returned ->
            log.warn("UNROUTABLE msg -> exchange={}, routingKey={}, replyCode={}, replyText={}",
                    returned.getExchange(), returned.getRoutingKey(),
                    returned.getReplyCode(), returned.getReplyText()));
        return t;
    }
}
