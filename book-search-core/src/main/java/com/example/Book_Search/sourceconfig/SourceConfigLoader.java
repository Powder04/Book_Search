package com.example.Book_Search.sourceconfig;

import java.io.*;
import java.util.*;
import org.springframework.core.io.*;
import org.yaml.snakeyaml.Yaml;

public final class SourceConfigLoader {

    private SourceConfigLoader() {}

    @SuppressWarnings("unchecked")
    public static List<SourceDefinition> load(String location) {
        ResourceLoader loader = new DefaultResourceLoader();
        Resource resource = loader.getResource(location);

        if (!resource.exists()) {
            throw new IllegalStateException("Khong tim thay file cau hinh nguon: " + location);
        }

        List<SourceDefinition> result = new ArrayList<>();

        try (InputStream in = resource.getInputStream()) {
            Yaml yaml = new Yaml();
            Map<String, Object> root = yaml.load(in);

            if (root == null || !root.containsKey("sources")) {
                return result;
            }

            List<Map<String, Object>> rawSources = (List<Map<String, Object>>) root.get("sources");
            for (Map<String, Object> raw : rawSources) {
                result.add(parseOne(raw));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Loi doc file cau hinh nguon: " + location, e);
        }

        return result;
    }

    private static SourceDefinition parseOne(Map<String, Object> raw) {
        String type = str(raw, "type", null);
        if (type == null) {
            throw new IllegalArgumentException("Thieu 'type' cho nguon: " + raw.get("name"));
        }

        return switch (type.toUpperCase()) {
            case "API" -> parseApi(raw);
            case "CRAWL" -> parseCrawl(raw);
            default -> throw new IllegalArgumentException(
                    "type khong ho tro: " + type + " (chi ho tro API hoac CRAWL)");
        };
    }

    @SuppressWarnings("unchecked")
    private static ApiSourceDefinition parseApi(Map<String, Object> raw) {
        ApiSourceDefinition def = new ApiSourceDefinition();
        fillCommon(def, raw);
        def.setBaseUrl(str(raw, "baseUrl", ""));
        def.setQueryTemplate(str(raw, "queryTemplate", ""));
        def.setItemsPath(str(raw, "itemsPath", "/"));

        Map<String, Object> fieldsRaw = (Map<String, Object>) raw.getOrDefault("fields", Map.of());
        Map<String, ApiFieldMapping> fields = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : fieldsRaw.entrySet()) {
            fields.put(e.getKey(), parseApiField((Map<String, Object>) e.getValue()));
        }
        def.setFields(fields);
        return def;
    }

    private static ApiFieldMapping parseApiField(Map<String, Object> raw) {
        ApiFieldMapping f = new ApiFieldMapping();
        f.setPath(str(raw, "path", null));
        f.setMode(ApiFieldMapping.Mode.valueOf(str(raw, "mode", "VALUE").toUpperCase()));
        f.setSeparator(str(raw, "separator", ", "));
        f.setSubPath(str(raw, "subPath", null));
        f.setTemplate(str(raw, "template", null));
        return f;
    }

    @SuppressWarnings("unchecked")
    private static CrawlSourceDefinition parseCrawl(Map<String, Object> raw) {
        CrawlSourceDefinition def = new CrawlSourceDefinition();
        fillCommon(def, raw);
        def.setListUrlTemplate(str(raw, "listUrlTemplate", ""));
        def.setPaginationSelector(str(raw, "paginationSelector", null));
        def.setPaginationMode(CrawlSourceDefinition.PaginationMode.valueOf(
                str(raw, "paginationMode", "NONE").toUpperCase()));
        def.setItemSelector(str(raw, "itemSelector", ""));
        def.setStaticLanguage(str(raw, "language", ""));
        def.setUserAgent(str(raw, "userAgent", "Mozilla/5.0"));
        def.setTimeoutMs(intVal(raw, "timeoutMs", 20000));
        def.setStrictTitleFilterThreshold(intVal(raw, "strictTitleFilterThreshold", 0));

        Map<String, Object> fieldsRaw = (Map<String, Object>) raw.getOrDefault("fields", Map.of());
        Map<String, FieldSelector> fields = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : fieldsRaw.entrySet()) {
            fields.put(e.getKey(), parseFieldSelector((Map<String, Object>) e.getValue()));
        }
        def.setFields(fields);
        return def;
    }

    private static FieldSelector parseFieldSelector(Map<String, Object> raw) {
        FieldSelector f = new FieldSelector();
        f.setSelector(str(raw, "selector", ""));
        f.setExtract(FieldSelector.ExtractType.valueOf(str(raw, "extract", "TEXT").toUpperCase()));
        f.setAttr(str(raw, "attr", null));
        f.setUrlPrefix(str(raw, "urlPrefix", ""));
        return f;
    }

    private static void fillCommon(SourceDefinition def, Map<String, Object> raw) {
        def.setName(str(raw, "name", null));
        def.setRoutingKey(str(raw, "routingKey", null));
        def.setQueueName(str(raw, "queueName", null));

        if (def.getName() == null || def.getRoutingKey() == null || def.getQueueName() == null) {
            throw new IllegalArgumentException(
                    "Nguon thieu name/routingKey/queueName trong cau hinh: " + raw);
        }
    }

    private static String str(Map<String, Object> m, String key, String def) {
        Object v = m.get(key);
        return v == null ? def : v.toString();
    }

    private static int intVal(Map<String, Object> m, String key, int def) {
        Object v = m.get(key);
        return v == null ? def : Integer.parseInt(v.toString());
    }
}
