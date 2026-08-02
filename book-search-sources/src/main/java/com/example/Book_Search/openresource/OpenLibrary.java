package com.example.Book_Search.openresource;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.example.Book_Search.model.BookSearch;
import com.example.Book_Search.model.OpenResource;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
@Component
public class OpenLibrary implements OpenResource {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public List<BookSearch> search(String keyword) {
        String url = "https://openlibrary.org/search.json?q=" + keyword + "&mode=ebooks&has_fulltext=true";

        String json = restTemplate.getForObject(url, String.class);

        List<BookSearch> books = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode docs = root.path("docs");

            for (JsonNode doc : docs) {
                // Lay tieu de sach
                String title = "";
                if (doc.has("title"))
                    title = doc.path("title").asText("");

                // Lay danh sach tac gia
                String author = "";
                if (doc.has("author_name")) {
                    JsonNode authors = doc.path("author_name");
                    List<String> authorList = new ArrayList<>();
                    for (JsonNode auth : authors)
                        authorList.add(auth.asText());

                    author = String.join(", ", authorList);
                }

                // Lay nam xuat ban
                String publishYear = "";
                if (doc.has("first_publish_year"))
                    publishYear = doc.path("first_publish_year").asText("");

                // Lay ngon ngu
                String language = "";
                if (doc.has("language"))
                    language = doc.path("language").get(0).asText("");

                // Lay hinh anh sach
                String coverUrl = "";
                if (doc.has("cover_i"))
                    coverUrl = "https://covers.openlibrary.org/b/id/" + doc.path("cover_i").asText("") + "-M.jpg";

                // Lay ma ISBN cua sach
                String isbn = "";
                if (doc.has("isbn"))
                    isbn = doc.path("isbn").get(0).asText("");

                // URL cua sach
                String bookUrl = "https://openlibrary.org/" + doc.path("key").asText("");

                books.add(new BookSearch(
                        title,
                        author,
                        publishYear,
                        language,
                        coverUrl,
                        isbn,
                        bookUrl,
                        "Open Library"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return books;
    }

    @Override
    public String getSourceName() {
        return "Open Library";
    }

    @Override
    public String getRoutingKey() {
        return "search.openlibrary";
    }

    @Override
    public String getQueueName() {
        return "openlibrary.queue";
    }
}
