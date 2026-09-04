package com.example.Book_Search.openresource;

import java.text.Normalizer;
import java.util.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import com.example.Book_Search.model.*;
import com.example.Book_Search.sourceconfig.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GenericCrawlResource implements OpenResource {
    private final CrawlSourceDefinition def;

    public GenericCrawlResource(CrawlSourceDefinition def) {
        this.def = def;
    }

    @Override
    public List<BookSearch> search(String keyword) {
        List<BookSearch> books = new ArrayList<>();
        try {
            String keywordEncoded = keyword.replace(" ", "+");
            Document firstPage = fetch(keywordEncoded, 1);

            int totalPages = 1;
            int paginationItemCount = 0;

            if (def.getPaginationMode() != CrawlSourceDefinition.PaginationMode.NONE
                    && def.getPaginationSelector() != null) {
                Elements paginationItems = firstPage.select(def.getPaginationSelector());
                paginationItemCount = paginationItems.size();
                totalPages = resolveTotalPages(paginationItems);
            }

            boolean strictFilter = def.getStrictTitleFilterThreshold() > 0
                    && paginationItemCount > def.getStrictTitleFilterThreshold();
            String keywordNorm = normalize(keyword);

            for (int page = 1; page <= totalPages; page++) {
                Document doc = (page == 1) ? firstPage : fetch(keywordEncoded, page);
                Elements items = doc.select(def.getItemSelector());

                for (Element item : items) {
                    String title = extract(item, "title");

                    if (strictFilter && !normalize(title).contains(keywordNorm)) {
                        continue;
                    }

                    books.add(buildBook(item, title));
                }
            }
        } catch (Exception e) {
            log.error("[{}] Loi khi crawl: {}", def.getName(), e.getMessage(), e);
        }
        return books;
    }

    private int resolveTotalPages(Elements paginationItems) {
        if (paginationItems.size() < 2) {
            return 1;
        }

        String text = def.getPaginationMode() == CrawlSourceDefinition.PaginationMode.LAST_TEXT
                ? paginationItems.get(paginationItems.size() - 1).text()
                : paginationItems.get(paginationItems.size() - 2).text();

        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private Document fetch(String keywordEncoded, int page) throws Exception {
        String url = def.getListUrlTemplate()
                .replace("{keyword}", keywordEncoded)
                .replace("{page}", String.valueOf(page));

        return Jsoup.connect(url)
                .userAgent(def.getUserAgent())
                .timeout(def.getTimeoutMs())
                .get();
    }

    private BookSearch buildBook(Element item, String title) {
        return new BookSearch(
                title,
                extract(item, "author"),
                extract(item, "publishYear"),
                def.getStaticLanguage(),
                extract(item, "coverUrl"),
                extract(item, "isbn"),
                extract(item, "bookUrl"),
                def.getName());
    }

    private String extract(Element item, String fieldName) {
        FieldSelector fs = def.getFields().get(fieldName);
        if (fs == null || fs.getSelector() == null || fs.getSelector().isBlank()) {
            return "";
        }

        Element el = item.selectFirst(fs.getSelector());
        if (el == null) {
            return "";
        }

        String raw = fs.getExtract() == FieldSelector.ExtractType.ATTR
                ? el.attr(fs.getAttr())
                : el.text();

        String prefix = fs.getUrlPrefix();
        return (prefix == null || raw.isBlank()) ? raw : prefix + raw;
    }

    private String normalize(String str) {
        if (str == null) {
            return "";
        }
        String normalized = Normalizer.normalize(str, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "").toLowerCase().trim();
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
