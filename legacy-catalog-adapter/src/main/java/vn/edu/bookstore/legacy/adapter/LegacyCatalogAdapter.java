package vn.edu.bookstore.legacy.adapter;

import org.springframework.stereotype.Service;
import vn.edu.bookstore.contracts.catalog.Book;
import vn.edu.bookstore.contracts.catalog.CurrencyCode;
import vn.edu.bookstore.contracts.catalog.Money;
import vn.edu.bookstore.legacy.adapter.soap.LegacyCatalogClient;

@Service
public class LegacyCatalogAdapter {

    private final LegacyCatalogClient client;

    public LegacyCatalogAdapter(LegacyCatalogClient client) {
        this.client = client;
    }

    public Book getBook(String isbn) {
        try {
            var legacyBook = client.getBook(isbn);
            if (legacyBook == null) {
                return null;
            }
            Book book = new Book();
            book.setIsbn(legacyBook.getIsbn());
            book.setTitle(legacyBook.getTitle());
            book.setAuthor(legacyBook.getAuthor());
            book.setAuthorId(legacyBook.getAuthorId());
            book.setPublishedYear(legacyBook.getPublishedYear());

            // Đổi giá trị source thành chữ thường "legacy-soap"
            book.setSource("legacy-soap");

            if (legacyBook.getPrice() != null) {
                Money money = new Money();
                money.setAmount(legacyBook.getPrice().getAmount());
                if (legacyBook.getPrice().getCurrency() != null) {
                    String currStr = legacyBook.getPrice().getCurrency().value();
                    money.setCurrency(CurrencyCode.fromValue(currStr));
                }
                book.setPrice(money);
            }
            return book;
        } catch (Exception e) {
            return null;
        }
    }
}
