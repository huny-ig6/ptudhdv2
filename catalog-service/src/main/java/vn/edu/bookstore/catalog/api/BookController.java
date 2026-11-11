package vn.edu.bookstore.catalog.api;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.bookstore.catalog.api.generated.BooksApi;
import vn.edu.bookstore.catalog.api.error.PreconditionRequiredException;
import vn.edu.bookstore.catalog.api.error.PreconditionFailedException;
import vn.edu.bookstore.catalog.api.model.Book;
import vn.edu.bookstore.catalog.api.model.BookCreateRequest;
import vn.edu.bookstore.catalog.api.model.BookPage;
import vn.edu.bookstore.catalog.api.model.BookUpdateRequest;
import vn.edu.bookstore.catalog.domain.BookEntity;
import vn.edu.bookstore.catalog.service.BookService;
import java.net.URI;

@RestController
public class BookController implements BooksApi {

    private final BookService service;

    public BookController(BookService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<BookPage> listBooks(String q, Integer page, Integer size, String sort) {
        return ResponseEntity.ok(BookMapper.toPage(service.list(q,
            PageRequest.of(page, size, toSort(sort)))));
    }

    static Sort toSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.ASC, "title").and(Sort.by("isbn"));
        }
        String[] parts = sort.split(",");
        String property = switch (parts[0]) {
            case "price" -> "priceAmount";
            case "publishedYear" -> "publishedYear";
            default -> "title";
        };

        Sort.Direction dir = parts.length > 1 && parts[1].equalsIgnoreCase("desc")
            ? Sort.Direction.DESC
            : Sort.Direction.ASC;

        return Sort.by(dir, property).and(Sort.by("isbn"));
    }

    @Override
    public ResponseEntity<Book> createBook(BookCreateRequest request) {
        BookEntity e = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/books/" + e.getIsbn()))
            .eTag(BookMapper.etag(e))
            .body(BookMapper.toApi(e));
    }

    @Override
    public ResponseEntity<Book> getBook(String isbn, String ifNoneMatch) {
        BookEntity e = service.get(isbn);
        String etag = BookMapper.etag(e);

        if (ifNoneMatch != null && matches(ifNoneMatch, etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build();
        }
        return ResponseEntity.ok().eTag(etag).body(BookMapper.toApi(e));
    }

    @Override
    public ResponseEntity<Book> updateBook(String isbn, BookUpdateRequest request, String ifMatch) {
        if (ifMatch == null || ifMatch.isBlank()) {
            throw new PreconditionRequiredException("Cần header If-Match chứa ETag đã đọc trước khi cập nhật");
        }
        BookEntity e = service.update(isbn, request, parseVersion(ifMatch));
        return ResponseEntity.ok().eTag(BookMapper.etag(e)).body(BookMapper.toApi(e));
    }

    @Override
    public ResponseEntity<Void> deleteBook(String isbn) {
        service.delete(isbn);
        return ResponseEntity.noContent().build();
    }

    private static boolean matches(String header, String etag) {
        for (String candidate : header.split(",")) {
            String c = candidate.trim();
            if (c.equals("*") || c.replaceFirst("^W/", "").equals(etag)) {
                return true;
            }
        }
        return false;
    }

    private static long parseVersion(String ifMatch) {
        String v = ifMatch.trim().replaceFirst("^W/", "").replace("\"", "");
        try {
            return Long.parseLong(v);
        } catch (NumberFormatException ex) {
            throw new PreconditionFailedException("If-Match không hợp lệ: " + ifMatch);
        }
    }
}
