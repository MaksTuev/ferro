package com.agna.ferro.sample.ui.screen.book;

import com.agna.ferro.mvp.component.scope.PerScreen;

import dagger.Module;
import dagger.Provides;

/**
 * module for Book screen
 */
@Module
class BookScreenModule {
    private String bookId;

    public BookScreenModule(String bookId) {
        this.bookId = bookId;
    }

    @Provides
    @PerScreen
    public String provideBookId(){
        return bookId;
    }
}
