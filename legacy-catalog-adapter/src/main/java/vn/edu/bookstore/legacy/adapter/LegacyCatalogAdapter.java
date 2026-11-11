package vn.edu.bookstore.legacy.adapter;

import java.math.BigInteger;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.springframework.stereotype.Service;

import vn.edu.bookstore.contracts.catalog.Book;
import vn.edu.bookstore.contracts.catalog.CurrencyCode;
import vn.edu.bookstore.contracts.catalog.Money;
import vn.edu.bookstore.legacy.adapter.soap.LegacyCatalogClient;
import vn.edu.bookstore.legacy.adapter.soap.LegacyBook;

@Service
public class LegacyCatalogAdapter {

    private final LegacyCatalogClient client;

    public LegacyCatalogAdapter(LegacyCatalogClient client) {
        this.client = client;
    }

    public Book getBook(String isbn) {
        try {
            LegacyBook legacyBook = client.getBook(isbn);

            if (legacyBook == null) {
                return null;
            }

            Book book = new Book();

            book.setIsbn(legacyBook.isbn());
            book.setTitle(legacyBook.title());
            book.setAuthor(legacyBook.author());

            // Integer -> BigInteger
            if (legacyBook.authorId() != null && legacyBook.authorId() > 0) {
                book.setAuthorId(
                    BigInteger.valueOf(legacyBook.authorId())
                );
            }

            // Integer -> XMLGregorianCalendar (xs:gYear)
            if (legacyBook.publishedYear() != null) {
                XMLGregorianCalendar year =
                    DatatypeFactory.newInstance()
                        .newXMLGregorianCalendarDate(
                            legacyBook.publishedYear(),
                            DatatypeConstants.FIELD_UNDEFINED,
                            DatatypeConstants.FIELD_UNDEFINED,
                            DatatypeConstants.FIELD_UNDEFINED
                        );

                book.setPublishedYear(year);
            }

            // Chuyển thông tin giá tiền
            if (legacyBook.amount() != null) {
                Money money = new Money();

                money.setAmount(legacyBook.amount());

                if (legacyBook.currency() != null) {
                    money.setCurrency(
                        CurrencyCode.fromValue(legacyBook.currency())
                    );
                }

                book.setPrice(money);
            }

            return book;

        } catch (Exception e) {
            return null;
        }
    }
}
