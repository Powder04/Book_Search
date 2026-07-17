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
public class ProjectGutenberg implements OpenResource {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public List<BookSearch> search(String keyword) {
        String url = "https://gutendex.com/books/?search=" + keyword;

        String json = restTemplate.getForObject(url, String.class);

        List<BookSearch> books = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode docs = root.path("results");

            for(JsonNode doc : docs) {
                // Lay tieu de sach
                String title = "";
                if(doc.has("title"))
                    title = doc.path("title").asText("");

                // Lay danh sach tac gia
                String author = "";
                if(doc.has("authors")) {
                    JsonNode authors = doc.path("authors");
                    List<String> authorList = new ArrayList<>();
                    for(JsonNode auth : authors) 
                        authorList.add(auth.path("name").asText());

                    author = String.join(", ", authorList);
                }

                // Lay nam xuat ban
                String publishYear = "";
                if(doc.has("publish"))
                    publishYear = doc.path("publish").asText("");

                // Lay ngon ngu
                String language = "";
                if(doc.has("languages")) 
                    language = doc.path("languages").get(0).asText("");

                // Lay ma ISBN cua sach
                String isbn = "";
                if(doc.has("isbn"))
                    isbn = doc.path("isbn").get(0).asText("");

                // URL va hinh anh cua sach
                String bookUrl = "";
                String coverUrl = "";
                if(doc.has("id")) {
                    bookUrl = "https://www.gutenberg.org/ebooks/" + doc.path("id").asText("");
                    coverUrl = "https://www.gutenberg.org/cache/epub/" 
                                + doc.path("id").asText("") 
                                + "/pg" 
                                + doc.path("id").asText("") 
                                + ".cover.medium.jpg";
                }

                books.add(new BookSearch(
                    title,
                    author,
                    publishYear,
                    language,
                    coverUrl,
                    isbn,
                    bookUrl,
                    "Project Gutenberg"));
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return books;
    }

    @Override
    public String getSourceName() {
        return "Project Gutenberg";
    }

    @Override
    public String getRoutingKey() {
        return "search.projectgutenberg";
    }

    @Override
    public String getQueueName() {
        return "projectgutenberg.queue";
    } 
}
