package com.agna.ferro.sample.ui.screen.catalog.grid;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.agna.ferro.sample.domain.entity.Book;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for books
 */
public class BooksAdapter extends RecyclerView.Adapter<BookHolder> {

    private List<Book> books = new ArrayList<>();
    private final BookItemListener itemListener;

    public BooksAdapter(BookItemListener bookItemListener) {
        this.itemListener = bookItemListener;
    }

    @Override
    public BookHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return BookHolder.newInstance(parent, itemListener);
    }

    @Override
    public void onBindViewHolder(BookHolder holder, int position) {
        holder.bind(books.get(position));
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    public void updateBooksData(List<Book> books) {
        this.books = books;
    }
}
