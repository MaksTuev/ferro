package com.agna.ferro.sample.ui.screen.catalog.grid;

import com.agna.ferro.sample.domain.Book;

public interface BookItemListener {
    void onDownloadClick(Book book);
    void onReadClick(Book book);
    void onClick(Book book);
}
