package com.agna.ferro.sample.ui.screen.catalog;

import com.agna.ferro.core.PersistentScreenScope;
import com.agna.ferro.mvp.component.provider.ActivityProvider;
import com.agna.ferro.mvp.component.scope.PerScreen;
import com.agna.ferro.mvprx.MvpRxPresenter;
import com.agna.ferro.rx.OperatorFreeze;
import com.agna.ferro.sample.domain.entity.Book;
import com.agna.ferro.sample.module.book.BookRepository;
import com.agna.ferro.sample.ui.navigation.Navigator;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

/**
 * Presenter for Catalog screen
 * <p>
 * <p>
 * Presenter with freeze logic.
 * If subscribe to {@link Observable} via one of {@link #subscribe(Observable, Subscriber)} method,
 * all rx events (onNext, onError, onComplete) would be frozen when view destroyed and unfrozen
 * when view recreated (see {@link OperatorFreeze}).
 * All events would be also frozen when screen paused and unfrozen when screen resumed.
 * When screen finally destroyed, all subscriptions would be automatically unsubscribed.
 * <p>
 * When configuration changed, presenter isn't destroyed and reused for new view
 */

@PerScreen
class CatalogPresenter extends MvpRxPresenter<CatalogActivityView> {

    /**
     * Example of object, which used {@link ActivityProvider}
     * This object retained in {@link PersistentScreenScope} together
     * with CatalogPresenter
     */
    private final Navigator navigator;
    private final BookRepository bookRepository;


    private List<Book> books = new ArrayList<>();
    private Subscription loadBookSubscription;

    @Inject
    public CatalogPresenter(BookRepository bookRepository, Navigator navigator) {
        this.bookRepository = bookRepository;
        this.navigator = navigator;
    }

    @Override
    public void onLoad(boolean viewRecreated) {
        super.onLoad(viewRecreated);
        tryLoadData();
        if (!viewRecreated) {
            observeChangingBooks();
        }
    }

    /**
     * example of request, which load main data for screen
     */
    private void tryLoadData() {
        if (books.size() != 0) {
            //if books already loaded, just show data
            onLoadBooksSuccess(books);
        } else if (isSubscriptionInactive(loadBookSubscription)) {
            //if data isn't loading now, start loading
            getView().showLoading();
            loadData();
        } else {
            //else simple wait while data has loaded
            getView().showLoading();
        }
    }

    /**
     * example of simple request
     * you do not check already loaded data and loading status
     */
    public void reloadData() {
        loadData();
    }

    private void loadData() {
        Observable<List<Book>> observable = bookRepository.getBooks()
                .observeOn(AndroidSchedulers.mainThread());
        loadBookSubscription = subscribe(observable,
                this::onLoadBooksSuccess,
                this::onLoadBooksError);
    }

    /**
     * example for subscribing to observable, which emits many events
     */
    private void observeChangingBooks() {
        Observable<Book> observable = bookRepository.observeChangingBooks()
                .observeOn(AndroidSchedulers.mainThread());
        subscribe(observable,
                //Keep only last book with different id in freeze buffer.
                //This prevent handling not relevant events when buffer would be unfrozen.
                //You can simple unsubscribe/subscrube to this observable and not use
                //  replaceFrozenEventPredicate, but then you can miss important event
                (frozenBook, newBook) -> frozenBook.getId().equals(newBook.getId()),
                this::updateBook,
                e -> Timber.e(e, "load data error"));

    }

    private void updateBook(Book newBook) {
        for(int i = 0; i<books.size(); i++){
            Book book = books.get(i);
            if (book.getId().equals(newBook.getId())) {
                books.set(i, newBook);
                getView().updateBooksData(books);
                getView().notifyItemChanged(i);
                break;
            }

        }
    }

    public void downloadBook(Book book) {
        bookRepository.startDownload(book.getId());
    }

    public void openBookScreen(Book book) {
        navigator.openBookScreen(book.getId());
    }

    private void onLoadBooksError(Throwable e) {
        getView().hideLoading();
        Timber.e(e, "load data error");
        //handle error
    }

    private void onLoadBooksSuccess(List<Book> books) {
        this.books = books;
        getView().hideLoading();
        getView().updateBooksData(books);
        getView().notifyDataChanged();
    }

}
