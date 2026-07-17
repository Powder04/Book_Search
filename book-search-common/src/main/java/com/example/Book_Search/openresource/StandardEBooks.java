package com.example.Book_Search.openresource;

import java.util.ArrayList;
import java.util.List;
import java.text.Normalizer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import com.example.Book_Search.model.BookSearch;
import com.example.Book_Search.model.OpenResource;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StandardEBooks implements OpenResource {

    @Override
    public List<BookSearch> search(String keyword) {
        List<BookSearch> books = new ArrayList<>();

        try {
            String keywordLast = keyword.replace(" ", "+");
            Document doc_page = Jsoup.connect("https://standardebooks.org/ebooks?query=" + keywordLast)
                    .userAgent("Mozilla/5.0")
                    .timeout(20000)
                    .get();

            int secondLast = 1, page = 1;

            Elements items = doc_page.select("nav.pagination ol li");
            if (items.size() >= 2) {
                String page_tmp = items.get(items.size() - 1).text();
                secondLast = Integer.parseInt(page_tmp);
            }
            boolean strictFilter = items.size() > 10;
            String keywordNorm = normalize(keyword);

            // Loop qua cac page
            while (page <= secondLast) {

                Document doc = Jsoup.connect(
                        "https://standardebooks.org/ebooks?page=" + page + "&query=" + keywordLast)
                        .userAgent("Mozilla/5.0")
                        .timeout(20000)
                        .get();

                Elements posts = doc.select("ol.ebooks-list li");

                for (Element post : posts) {
                    // Title
                    Element titleEl = post.selectFirst("p a span");
                    String title = titleEl != null ? titleEl.text() : "";

                    if(strictFilter) {
                        String titleNorm = normalize(title);

                        if(!titleNorm.contains(keywordNorm)) {
                            continue;
                        }
                    }

                    // Author
                    Element authorEl = post.selectFirst("p.author a span");
                    String author = authorEl != null ? authorEl.text() : "";

                    // Image's link
                    Element imgEl = post.selectFirst("div.thumbnail-container img");
                    String coverUrl = imgEl != null ? "https://standardebooks.org" + imgEl.attr("src") : "";

                    // Book's link
                    Element linkEl = post.selectFirst("div.thumbnail-container a");
                    String bookUrl = linkEl != null ? "https://standardebooks.org" + linkEl.attr("href") : "";

                    books.add(new BookSearch(
                            title,
                            author,
                            "",
                            "",
                            coverUrl,
                            "",
                            bookUrl,
                            "Standard EBooks"));
                }
                page++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return books;
    }

    @Override
    public String getSourceName() {
        return "Standard EBooks";
    }

    private String normalize(String str) {
        if (str == null) return "";
        String normalized = Normalizer.normalize(str, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "")
                .toLowerCase()
                .trim();
    }

    @Override
    public String getRoutingKey() {
        return "search.standardebooks";
    }

    @Override
    public String getQueueName() {
        return "standardebooks.queue";
    }
}