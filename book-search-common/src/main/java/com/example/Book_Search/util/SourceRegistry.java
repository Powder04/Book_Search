package com.example.Book_Search.util;

import java.util.*;
import org.springframework.stereotype.Component;
import com.example.Book_Search.model.OpenResource;

@Component
public class SourceRegistry {

    private final Map<String, OpenResource> sources;

    public SourceRegistry(List<OpenResource> resources) {
        this.sources = new HashMap<>();

        for (OpenResource resource : resources) {
            String routingKey = resource.getRoutingKey();
            this.sources.put(routingKey, resource);
        }
    }

    public List<OpenResource> getAllSources() {
        List<OpenResource> result = new ArrayList<>();

        for (OpenResource resource : sources.values()) 
            result.add(resource);

        return result;
    }

    public OpenResource getByRoutingKey(String routingKey) {
        OpenResource resource = sources.get(routingKey);
        return resource;
    }
}
