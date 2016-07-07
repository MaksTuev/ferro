package com.agna.ferro.sample.module.book;

import android.content.Context;

import com.agna.ferro.mvp.component.scope.PerApplication;
import com.agna.ferro.sample.R;
import com.agna.ferro.sample.domain.entity.Book;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Repository of books
 */
@PerApplication
public class BookRepository {

    private final Context appContext;

    private CopyOnWriteArrayList<Book> books;
    private PublishSubject<Book> changingBookSubject = PublishSubject.create();

    @Inject
    public BookRepository(Context appContext) {
        this.appContext = appContext;
    }

    /**
     * @return all books
     */
    public Observable<List<Book>> getBooks() {
        return Observable.timer(3, TimeUnit.SECONDS)
                .flatMap(t -> Observable.just(books));
    }

    /**
     * @return book with bookId
     */
    public Observable<Book> getBook(String bookId) {
        return Observable.timer(3, TimeUnit.SECONDS)
                .flatMap(t -> Observable.from(books))
                .filter(book -> book.getId().equals(bookId))
                .first();
    }

    /**
     * @return description for book with bookId
     */
    public Observable<String> getBookDescription(String bookId) {
        return Observable.just(appContext.getString(R.string.test_book_description));
    }

    /**
     * start download book, events about updating book can receive via {@link #observeChangingBooks()}
     */
    public void startDownload(String bookId) {
        imitateDownloading(bookId);
    }

    private void imitateDownloading(String bookId) {
        List<Integer> percents = Arrays.asList(5, 17, 33, 50, 66, 81, 92, 100);
        Observable.zip(
                Observable.interval(600, TimeUnit.MILLISECONDS),
                Observable.from(percents),
                (t, percent) -> percent)
                .doOnNext(percent -> updateBook(bookId, percent))
                .subscribe();


    }

    /**
     * emit event, when book changed
     */
    public Observable<Book> observeChangingBooks() {
        return changingBookSubject;
    }

    private void updateBook(String bookId, int downloadProgress) {
        for (Book book : books) {
            if (book.getId().equals(bookId)) {
                book.setDownloadProgress(downloadProgress);
                changingBookSubject.onNext(book);
                break;
            }
        }
    }

    // ----- generate test data -----

    private Random rnd = new Random();

    {
        books = createBooks();
    }

    private CopyOnWriteArrayList<Book> createBooks() {
        CopyOnWriteArrayList<Book> result = new CopyOnWriteArrayList<>();
        result.add(createBook(
                "STREET ART: The Punk Poster in San Francisco 1977-1981 by Peter Belsito, et al.",
                "http://bookshop.europa.eu/is-bin/intershop.static/WFS/EU-Bookshop-Site/EU-Bookshop/en_GB/QC0213820.jpg"));
        result.add(createBook(
                "TECHNOLOGY AND COSMOGENESIS by Paolo Soleri",
                "http://cdn.shopify.com/s/files/1/0880/2454/products/Soleri-1_grande.jpg"));
        result.add(createBook(
                "CAPTAIN BRASSBOUND'S CONVERSION by G.B. Shaw (in Czech with cover by Ladislav Sutnar)",
                "http://cdn.shopify.com/s/files/1/0880/2454/products/SUTNAR-1_grande.jpg?v=1460008835"));
        result.add(createBook(
                "DOMUS: 45 ANS D'ARCHITECTURE, DESIGN, ART by Gio Ponti, et al",
                "http://cdn.shopify.com/s/files/1/0880/2454/products/Domus-1_grande.jpg?v=1465493211"));
        result.add(createBook(
                "ELIOT AND HIS AGE by Russell Kirk",
                "http://cdn.shopify.com/s/files/1/0880/2454/products/ELIOT-1_grande.jpg?v=1454448503"));
        result.add(createBook(
                "FILM CULTURE ISSUE 30: METAPHORS ON VISION, Mekas, Brakhage, Maciunas, Fluxus",
                "http://cdn.shopify.com/s/files/1/0880/2454/products/Film_Culture-1_grande.jpg?v=1465413318"));
        result.add(createBook(
                "MOVIE JOURNAL: The Rise of the New American Cinema, 1959-1971 by Jonas Mekas",
                "http://cdn.shopify.com/s/files/1/0880/2454/products/Mekas-1_grande.jpg?v=1463726116"));
        result.add(createBook(
                "OEUVRES COMPLÉTES by Jean-Jacques Rousseau",
                "http://cdn.shopify.com/s/files/1/0880/2454/products/Rousseau1_grande.jpg?v=1443841168"));
        result.add(createBook(
                "PAGES by Joe Tilson",
                "http://cdn.shopify.com/s/files/1/0880/2454/products/Pages-1_grande.jpg?v=1463863668"));
        result.add(createBook(
                "QUICKER THAN THE EYE: The Magic and Magicians of the World by John Mulholland",
                "http://cdn.shopify.com/s/files/1/0880/2454/products/QUICKER-1_grande.jpg?v=1460754317"));
        result.add(createBook(
                "RANDALL JARRELL by Karl Shapiro",
                "http://cdn.shopify.com/s/files/1/0880/2454/products/JARRELL-1_grande.jpg?v=1459990471"));
        return result;
    }

    private Book createBook(String name, String imageUrl) {
        return new Book(UUID.randomUUID().toString(), name, imageUrl);
    }

}
