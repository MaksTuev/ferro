package com.agna.ferro.sample.ui.screen.book;

import com.agna.ferro.sample.domain.entity.Book;

/**
 * inner model, which used inside Book screen
 */
class FullBookModel {
    private Book book;
    private String description;

    public FullBookModel(Book book, String description) {
        this.book = book;
        this.description = description;
    }

    public Book getBook() {
        return book;
    }

    public String getDescription() {
        return description;
    }

    public void setBook(Book book) {
        this.book = book;
    }
}
