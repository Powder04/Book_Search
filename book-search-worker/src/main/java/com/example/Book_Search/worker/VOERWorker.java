package com.example.Book_Search.worker;

import java.util.List;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import com.example.Book_Search.config.RabbitMQProperties;
import com.example.Book_Search.model.BookSearch;
import com.example.Book_Search.model.SearchRequest;
import com.example.Book_Search.openresource.VOER;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class VOERWorker extends BaseWorker {
    private final VOER resource;

    public VOERWorker(RabbitTemplate rabbitTemplate, RabbitMQProperties properties, VOER resource) {
        super(rabbitTemplate, properties);
        this.resource = resource;
    }

    @RabbitListener(queues = "voer.queue", 
                    concurrency = "#{rabbitMQProperties.source.concurrency}",
                    containerFactory = "workerFactory")
    public void consume(SearchRequest request) {
        try {
            String keyword = request.getKeyword();
            String keywordTrans = request.getKeywordTrans();

            List<BookSearch> books = resource.search(keyword);
            publishBooks(request, resource, books);

            if (keywordTrans != null && !keywordTrans.isBlank() && !keywordTrans.equalsIgnoreCase(keyword)) {
                List<BookSearch> booksEN = resource.search(keywordTrans);
                publishBooks(request, resource, booksEN);
            }

        } catch(Exception e) {
            log.error(e.getMessage());
        } finally {
            publishDone(request, resource);
        }
    }
}
