package com.example.Book_Search.openresource;

import java.util.*;
import org.springframework.web.client.RestTemplate;
import com.example.Book_Search.model.*;
import com.example.Book_Search.sourceconfig.*;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.*;

@Slf4j
public class GenericApiResource implements OpenResource {
    private final ApiSourceDefinition def;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GenericApiResource(ApiSourceDefinition def, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.def = def;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<BookSearch> search(String keyword) {
        List<BookSearch> books = new ArrayList<>();
        try {
            String url = def.getBaseUrl() + def.getQueryTemplate().replace("{keyword}", keyword);
            String json = restTemplate.getForObject(url, String.class);
            if (json == null) {
                return books;
            }

            JsonNode root = objectMapper.readTree(json);
            JsonNode items = root.at(def.getItemsPath());

            for (JsonNode item : items) {
                Map<String, String> values = new HashMap<>();
                for (Map.Entry<String, ApiFieldMapping> e : def.getFields().entrySet()) {
                    values.put(e.getKey(), resolve(item, e.getValue()));
                }

                books.add(new BookSearch(
                        values.getOrDefault("title", ""),
                        values.getOrDefault("author", ""),
                        values.getOrDefault("publishYear", ""),
                        values.getOrDefault("language", ""),
                        values.getOrDefault("coverUrl", ""),
                        values.getOrDefault("isbn", ""),
                        values.getOrDefault("bookUrl", ""),
                        def.getName()));
            }
        } catch (Exception e) {
            log.error("[{}] Loi khi goi API: {}", def.getName(), e.getMessage(), e);
        }
        return books;
    }

    private String resolve(JsonNode item, ApiFieldMapping mapping) {
        if (mapping == null || mapping.getPath() == null) {
            return "";
        }

        JsonNode node = item.at(mapping.getPath());
        if (node.isMissingNode() || node.isNull()) {
            return "";
        }

        return switch (mapping.getMode()) {
            case VALUE -> firstOrText(node);
            case JOIN -> joinArray(node, mapping.getSeparator(), mapping.getSubPath());
            case TEMPLATE -> {
                String value = firstOrText(node);
                yield mapping.getTemplate() == null ? value : mapping.getTemplate().replace("{value}", value);
            }
        };
    }

    private String firstOrText(JsonNode node) {
        return node.isArray() && node.size() > 0 ? node.get(0).asText("") : node.asText("");
    }

    private String joinArray(JsonNode node, String separator, String subPath) {
        if (!node.isArray()) {
            return node.asText("");
        }

        List<String> parts = new ArrayList<>();
        for (JsonNode element : node) {
            JsonNode target = (subPath == null || subPath.isBlank()) ? element : element.at(subPath);
            if (!target.isMissingNode() && !target.isNull()) {
                parts.add(target.asText(""));
            }
        }
        return String.join(separator, parts);
    }

    @Override
    public String getSourceName() {
        return def.getName();
    }

    @Override
    public String getRoutingKey() {
        return def.getRoutingKey();
    }

    @Override
    public String getQueueName() {
        return def.getQueueName();
    }
}
