package com.agna.ferro.sample.ui.navigation;

import com.agna.ferro.mvp.component.provider.ActivityProvider;
import com.agna.ferro.mvp.component.scope.PerScreen;
import com.agna.ferro.sample.ui.screen.book.BookActivity;

import javax.inject.Inject;

/**
 * Encapsulate navigation between screens
 *
 * Example of class, which used {@link ActivityProvider}
 * Object of this class retained in {@link com.agna.ferro.core.PersistentScreenScope} because
 * it part of ScreenComponent
 */
@PerScreen
public class Navigator {

    private final ActivityProvider activityProvider;

    @Inject
    public Navigator(ActivityProvider activityProvider) {
        this.activityProvider = activityProvider;
    }

    public void openBookScreen(String bookId){
        BookActivity.start(activityProvider.get(), bookId);
    }
}
