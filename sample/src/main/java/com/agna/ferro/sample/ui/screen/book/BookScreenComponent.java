package com.agna.ferro.sample.ui.screen.book;

import com.agna.ferro.mvp.component.ScreenComponent;
import com.agna.ferro.mvp.component.scope.PerScreen;
import com.agna.ferro.sample.app.AppComponent;

import dagger.Component;

/**
 * component for Book screen
 */
@PerScreen
@Component(dependencies = AppComponent.class, modules = BookScreenModule.class)
interface BookScreenComponent extends ScreenComponent<BookFragmentView> {
}
