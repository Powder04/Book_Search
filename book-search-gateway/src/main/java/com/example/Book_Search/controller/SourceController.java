package com.example.Book_Search.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import com.example.Book_Search.dto.SourceMeta;
import com.example.Book_Search.sourceconfig.*;

@RestController
public class SourceController {

    private final List<SourceMeta> sources;

    public SourceController(
            @Value("${app.sources.config-path:classpath:sources-config.yaml}") String sourcesConfigPath) {
        List<SourceDefinition> definitions = SourceConfigLoader.load(sourcesConfigPath);
        this.sources = definitions.stream()
                .map(def -> new SourceMeta(def.getName(), slugify(def.getName())))
                .toList();
    }

    @GetMapping("/sources")
    public List<SourceMeta> getSources() {
        return sources;
    }

    private static String slugify(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9]+", "");
    }
}
