package com.example.Book_Search.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import com.example.Book_Search.config.WebSocketProperties;
import com.example.Book_Search.model.SearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class Listener {
    private final SimpMessagingTemplate messagingTemplate;
    private final WebSocketProperties webSocketProperties;

    @RabbitListener(queues = "#{gatewayResultQueue.name}",
                    containerFactory = "resultFactory")
    public void consume(SearchResult result) {
        messagingTemplate.convertAndSend(
                webSocketProperties.getTopicPrefix() + result.getSearchId(),
                result
        );
    }
}
