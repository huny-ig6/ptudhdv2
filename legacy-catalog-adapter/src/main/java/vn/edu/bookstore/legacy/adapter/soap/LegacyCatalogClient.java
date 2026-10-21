package vn.edu.bookstore.legacy.adapter.soap;

import java.util.List;
import org.springframework.stereotype.Component;
import vn.edu.bookstore.legacy.adapter.soap.generated.Book;
import vn.edu.bookstore.legacy.adapter.soap.generated.BookCatalogFault;
import vn.edu.bookstore.legacy.adapter.soap.generated.BookCatalogPortType;
import vn.edu.bookstore.legacy.adapter.soap.generated.GetBookRequest;
import vn.edu.bookstore.legacy.adapter.soap.generated.GetBookResponse;
import vn.edu.bookstore.legacy.adapter.soap.generated.SearchBooksRequest;
import vn.edu.bookstore.legacy.adapter.soap.generated.SearchBooksResponse;

@Component
public class LegacyCatalogClient {

    private final BookCatalogPortType port;

    public LegacyCatalogClient(BookCatalogPortType port) {
        this.port = port;
    }

    public Book getBook(String isbn) {
        try {
            GetBookRequest req = new GetBookRequest();
            req.setIsbn(isbn);
            GetBookResponse res = port.getBook(req);
            return res.getBook();
        } catch (BookCatalogFault e) {
            throw new RuntimeException(e);
        }
    }

    public List<Book> search(String keyword, int maxResults) {
        return searchBooks(keyword, maxResults);
    }

    public List<Book> searchBooks(String keyword, Integer maxResults) {
        try {
            SearchBooksRequest req = new SearchBooksRequest();
            req.setKeyword(keyword);
            if (maxResults != null) {
                req.setMaxResults(maxResults);
            }
            SearchBooksResponse res = port.searchBooks(req);
            return res.getBook();
        } catch (BookCatalogFault e) {
            throw new RuntimeException(e);
        }
    }
}
