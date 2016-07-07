package com.agna.ferro.sample.ui.screen.catalog;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.agna.ferro.mvp.component.ScreenComponent;
import com.agna.ferro.mvp.presenter.MvpPresenter;
import com.agna.ferro.sample.R;
import com.agna.ferro.sample.domain.entity.Book;
import com.agna.ferro.sample.ui.base.BaseActivityView;
import com.agna.ferro.sample.ui.dagger.ActivityModule;
import com.agna.ferro.sample.ui.screen.catalog.grid.BookItemListener;
import com.agna.ferro.sample.ui.screen.catalog.grid.BooksAdapter;

import java.util.List;

import javax.inject.Inject;

/**
 * view for Catalog screen
 */
public class CatalogActivityView extends BaseActivityView {

    @Inject
    CatalogPresenter presenter;

    private Handler handler = new Handler();
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView booksRw;
    private BooksAdapter adapter;

    @Override
    protected int getContentView() {
        return R.layout.activity_catalog;
    }

    @Override
    protected ScreenComponent createScreenComponent() {
        //creating Screen Component, you needn't call ScreenComponent#inject(this)
        return DaggerCatalogScreenComponent.builder()
                .appComponent(getAppComponent())
                .activityModule(new ActivityModule(getPersistentScreenScope()))
                .build();
    }

    @Override
    public MvpPresenter getPresenter() {
        return presenter;
    }

    @Override
    public String getName() {
        // name of the screen
        return "Catalog";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState, boolean viewRecreated) {
        super.onCreate(savedInstanceState, viewRecreated);
        findViews();
        initViews();
        initGrid();
    }

    public void showLoading() {
        handler.post(() -> swipeRefreshLayout.setRefreshing(true));
    }

    public void hideLoading() {
        swipeRefreshLayout.setRefreshing(false);
    }

    private void initViews() {
        swipeRefreshLayout.setOnRefreshListener(() -> presenter.reloadData());
    }

    private void initGrid() {
        int spanCount = getResources().getInteger(R.integer.books_span_count);
        GridLayoutManager layoutManager = new GridLayoutManager(this, spanCount);
        booksRw.setLayoutManager(layoutManager);
        booksRw.setItemAnimator(null);
        adapter = new BooksAdapter(bookItemListener);
        booksRw.setAdapter(adapter);
    }

    private void findViews() {
        booksRw = (RecyclerView) findViewById(R.id.catalog_rw);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.catalog_swr);
    }

    private BookItemListener bookItemListener = new BookItemListener() {
        @Override
        public void onDownloadClick(Book book) {
            presenter.downloadBook(book);
        }

        @Override
        public void onReadClick(Book book) {
            Toast.makeText(CatalogActivityView.this, "Stub", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onClick(Book book) {
            presenter.openBookScreen(book);
        }
    };

    public void notifyDataChanged() {
        adapter.notifyDataSetChanged();
    }

    public void updateBooksData(List<Book> books) {
        adapter.updateBooksData(books);
    }

    public void notifyItemChanged(int position) {
        adapter.notifyItemChanged(position);
    }

}
