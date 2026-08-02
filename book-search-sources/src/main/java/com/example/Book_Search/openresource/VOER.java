package com.example.Book_Search.openresource;

import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import com.example.Book_Search.model.BookSearch;
import com.example.Book_Search.model.OpenResource;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class VOER implements OpenResource {

    @Override
    public List<BookSearch> search(String keyword) {
        List<BookSearch> books = new ArrayList<>();
        try {
            String keywordLast = keyword.replace(" ", "+");
            Document doc_page = Jsoup.connect("https://voer.edu.vn/browse?title=" + keywordLast)
                    .userAgent("Mozilla/5.0")
                    .timeout(20000)
                    .get();

            int secondLast = 1, page = 1;

            // Lay tat ca the .page-item de xem co bao nhieu page
            Elements items = doc_page.select("ul.pagination li.page-item");
            if(items.size() >= 2) {
                String page_tmp = items.get(items.size() - 2).text();
                secondLast = Integer.parseInt(page_tmp);
            }

            // Lap qua cac page de them sach vao List
            while(page <= secondLast) {
                Document doc = Jsoup.connect("https://voer.edu.vn/browse?title=" + keywordLast + "&page=" + page)
                    .userAgent("Mozilla/5.0")
                    .timeout(20000)
                    .get();
                
                // Chon cac div chua thong tin sach
                Elements posts = doc.select("div.community-post");

                for(Element post : posts) {
                    // Lay tieu de sach
                    Element titleEl = post.selectFirst("h3.post-title");
                    String title = titleEl != null ? titleEl.text() : "";

                    // Lay danh sach tac gia
                    Element authorEl = post.selectFirst(".bbp-author-name");
                    String author = authorEl != null ? authorEl.text() : "";

                    // Lay nam xuat ban
                    String publishYear = post.selectFirst(".freshness-link span").text();

                    // Lay hinh anh sach
                    Element imgEl = post.selectFirst("div.image-cover img");
                    String coverUrl = imgEl != null ? imgEl.attr("src") : "";

                    // URL cua sach
                    Element linkEl = post.selectFirst(".entry-content a");
                    String bookUrl = linkEl != null ? linkEl.attr("href") : "";

                    books.add(new BookSearch(
                        title, 
                        author, 
                        publishYear, 
                        "VI", 
                        coverUrl, 
                        "", 
                        bookUrl, 
                        "VOER"));
                }
                page++;
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return books;
    }

    @Override
    public String getSourceName() {
        return "VOER";
    }

    @Override
    public String getRoutingKey() {
        return "search.voer";
    }

    @Override
    public String getQueueName() {
        return "voer.queue";
    }
}
