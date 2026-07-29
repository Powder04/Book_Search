package com.example.Book_Search.worker;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.example.Book_Search.config.RabbitMQProperties;
import com.example.Book_Search.model.*;
import lombok.AllArgsConstructor;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public abstract class BaseWorker {
    protected final RabbitTemplate rabbitTemplate;
    protected final RabbitMQProperties rabbitmqProperties;
    protected final Executor searchTaskExecutor;

    protected void executeWithTimeout(OpenResource resource, Runnable searchLogic) {
        try {
            CompletableFuture
                .runAsync(searchLogic, searchTaskExecutor)
                .get(20, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.warn("[{}] Timeout sau 20s, bo qua.", resource.getSourceName());
        } catch (Exception e) {
            log.error("[{}] Loi khi tim kiem: {}", resource.getSourceName(), e.getMessage(), e);
        }
    }

    protected void publishBooks(SearchRequest request, OpenResource resource, List<BookSearch> books) {
        for(BookSearch book : books) {
            SearchResult result = new SearchResult(
                                request.getSearchId(), 
                                resource.getSourceName(), 
                                book.getTitle(), 
                                book.getAuthor(), 
                                book.getPublishYear(), 
                                book.getLanguage(), 
                                book.getCoverUrl(), 
                                book.getIsbn(), 
                                book.getBookUrl(), 
                                "BOOKS");

            rabbitTemplate.convertAndSend(
                    rabbitmqProperties.getFanoutExchange(),
                    "",
                    result
            );
        }
    }

    protected void publishDone(SearchRequest request, OpenResource resource) {
        SearchResult done = new SearchResult(
                            request.getSearchId(), 
                            resource.getSourceName(), 
                            null, 
                            null, 
                            null, 
                            null, 
                            null, 
                            null, 
                            null, 
                            "DONE");

        rabbitTemplate.convertAndSend(
                rabbitmqProperties.getFanoutExchange(),
                "",
                done
        );
    }
}
