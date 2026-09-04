package com.example.Book_Search.config;

import java.util.List;
import java.util.concurrent.*;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import com.example.Book_Search.model.*;
import com.example.Book_Search.util.SourceRegistry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DynamicWorkerRegistrar implements RabbitListenerConfigurer {

    private static final long SEARCH_TIMEOUT_SECONDS = 20;

    private final SourceRegistry registry;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQProperties rabbitmqProperties;
    private final Executor searchTaskExecutor;
    private final MessageConverter messageConverter;
    private final SimpleRabbitListenerContainerFactory workerFactory;

    public DynamicWorkerRegistrar(SourceRegistry registry,
                                   RabbitTemplate rabbitTemplate,
                                   RabbitMQProperties rabbitmqProperties,
                                   @Qualifier("searchTaskExecutor") Executor searchTaskExecutor,
                                   MessageConverter messageConverter,
                                   @Qualifier("workerFactory") SimpleRabbitListenerContainerFactory workerFactory) {
        this.registry = registry;
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitmqProperties = rabbitmqProperties;
        this.searchTaskExecutor = searchTaskExecutor;
        this.messageConverter = messageConverter;
        this.workerFactory = workerFactory;
    }

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
        for (OpenResource resource : registry.getAllSources()) {
            SimpleRabbitListenerEndpoint endpoint = new SimpleRabbitListenerEndpoint();
            endpoint.setId(resource.getQueueName() + "-endpoint");
            endpoint.setQueueNames(resource.getQueueName());
            endpoint.setMessageListener(buildListener(resource));
            registrar.registerEndpoint(endpoint, workerFactory);

            log.info("Dang ky worker listener cho nguon [{}] tren queue [{}]",
                    resource.getSourceName(), resource.getQueueName());
        }
    }

    private MessageListener buildListener(OpenResource resource) {
        return (Message message) -> {
            Object converted = messageConverter.fromMessage(message);
            if (!(converted instanceof SearchRequest request)) {
                log.warn("[{}] Nhan message khong dung dinh dang SearchRequest, bo qua.",
                        resource.getSourceName());
                return;
            }
            handleWithTimeout(resource, request);
        };
    }

    private void handleWithTimeout(OpenResource resource, SearchRequest request) {
        try {
            CompletableFuture
                    .runAsync(() -> executeSearch(resource, request), searchTaskExecutor)
                    .get(SEARCH_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.warn("[{}] Timeout sau {}s, bo qua.", resource.getSourceName(), SEARCH_TIMEOUT_SECONDS);
        } catch (Exception e) {
            log.error("[{}] Loi khi tim kiem: {}", resource.getSourceName(), e.getMessage(), e);
        }
    }

    private void executeSearch(OpenResource resource, SearchRequest request) {
        try {
            String keyword = request.getKeyword();
            String keywordTrans = request.getKeywordTrans();

            List<BookSearch> books = resource.search(keyword);
            publishBooks(request, resource, books);

            if (keywordTrans != null && !keywordTrans.isBlank() && !keywordTrans.equalsIgnoreCase(keyword)) {
                List<BookSearch> booksEN = resource.search(keywordTrans);
                publishBooks(request, resource, booksEN);
            }
        } catch (Exception e) {
            log.error("[{}] Loi khi tim kiem: {}", resource.getSourceName(), e.getMessage(), e);
        } finally {
            publishDone(request, resource);
        }
    }

    private void publishBooks(SearchRequest request, OpenResource resource, List<BookSearch> books) {
        for (BookSearch book : books) {
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

            rabbitTemplate.convertAndSend(rabbitmqProperties.getFanoutExchange(), "", result);
        }
    }

    private void publishDone(SearchRequest request, OpenResource resource) {
        SearchResult done = new SearchResult(
                request.getSearchId(),
                resource.getSourceName(),
                null, null, null, null, null, null, null,
                "DONE");

        rabbitTemplate.convertAndSend(rabbitmqProperties.getFanoutExchange(), "", done);
    }
}
