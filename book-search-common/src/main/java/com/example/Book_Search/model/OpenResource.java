package com.example.Book_Search.model;

import java.util.List;

public interface OpenResource {
    List<BookSearch> search(String keyword);
    String getSourceName();
    String getRoutingKey();
    String getQueueName();
}
