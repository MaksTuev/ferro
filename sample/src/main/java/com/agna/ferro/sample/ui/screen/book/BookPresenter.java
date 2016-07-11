package com.agna.ferro.sample.ui.screen.book;

import com.agna.ferro.mvp.component.scope.PerScreen;
import com.agna.ferro.mvprx.MvpRxPresenter;
import com.agna.ferro.rx.OperatorFreeze;
import com.agna.ferro.sample.domain.entity.Book;
import com.agna.ferro.sample.module.book.BookRepository;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

/**
 * presenter for Book screen
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
class BookPresenter extends MvpRxPresenter<BookFragmentView> {

    private final BookRepository bookRepository;
    private final String bookId;

    private FullBookModel fullBookModel;
    private Subscription loadFullBookSubscription;

    @Inject
    public BookPresenter(BookRepository bookRepository, String bookId) {
        this.bookRepository = bookRepository;
        this.bookId = bookId;
    }

    @Override
    public void onLoad(boolean viewRecreated) {
        super.onLoad(viewRecreated);
        tryLoadData();
        if (!viewRecreated) {
            observeChangingBook();
        }
    }

    public void downloadBook() {
        if (fullBookModel != null && !fullBookModel.getBook().isDownloading()) {
            bookRepository.startDownload(bookId);
        }
    }

    /**
     * example of request, which load main data for screen
     */
    private void tryLoadData() {
        if (fullBookModel != null) {
            //if books already loaded, just show data
            onLoadDataSuccess(fullBookModel);
        } else if (isSubscriptionInactive(loadFullBookSubscription)) {
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
        Observable<FullBookModel> observable = Observable.zip(
                bookRepository.getBook(bookId),
                bookRepository.getBookDescription(bookId),
                FullBookModel::new)
                .observeOn(AndroidSchedulers.mainThread());

        loadFullBookSubscription = subscribe(observable,
                this::onLoadDataSuccess,
                this::onLoadDataError);
    }

    /**
     * example for subscribing to observable, which emits many events
     */
    private void observeChangingBook() {
        Observable<Book> observable = bookRepository.observeChangingBooks()
                //getting event only for book with bookId
                .filter(book -> book.getId().equals(bookId))
                .observeOn(AndroidSchedulers.mainThread());

        subscribe(observable,
                //Keep only last book in freeze buffer.
                //This prevent handling not relevant events when buffer would be unfrozen.
                //You can simple unsubscribe/subscrube to this observable and not use
                //  replaceFrozenEventPredicate, but then you can miss important event
                (frozenBook, newBook) -> true,
                this::updateBook,
                e -> Timber.e(e, "update book error"));

    }

    private void updateBook(Book newBook) {
        if (fullBookModel != null) {
            fullBookModel.setBook(newBook);
            getView().showData(fullBookModel);
        }
    }

    private void onLoadDataSuccess(FullBookModel fullBookModel) {
        this.fullBookModel = fullBookModel;
        getView().hideLoading();
        getView().showData(fullBookModel);
    }

    private void onLoadDataError(Throwable e) {
        getView().hideLoading();
        //handle error
    }


}
