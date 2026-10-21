package vn.edu.bookstore.legacy.adapter.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.bookstore.contracts.catalog.Book;
import vn.edu.bookstore.legacy.adapter.LegacyCatalogAdapter;

@RestController
@RequestMapping("/api/v1/legacy/books")
public class LegacyBookController {

    private final LegacyCatalogAdapter adapter;

    public LegacyBookController(LegacyCatalogAdapter adapter) {
        this.adapter = adapter;
    }

    @GetMapping("/{isbn}")
    public ResponseEntity<Book> getBook(@PathVariable("isbn") String isbn) {
        Book book = adapter.getBook(isbn);
        if (book == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(book);
    }
}
