package com.agna.ferro.sample.ui.screen.catalog;

import com.agna.ferro.mvp.component.ScreenComponent;
import com.agna.ferro.mvp.component.scope.PerScreen;
import com.agna.ferro.sample.app.AppComponent;
import com.agna.ferro.sample.ui.dagger.ActivityModule;

import dagger.Component;

/**
 * Component for Catalog screen
 */
@PerScreen
@Component(dependencies = AppComponent.class, modules = ActivityModule.class)
interface CatalogScreenComponent extends ScreenComponent<CatalogActivityView> {
}
