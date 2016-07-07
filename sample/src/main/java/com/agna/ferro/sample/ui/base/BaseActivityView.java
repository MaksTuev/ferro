package com.agna.ferro.sample.ui.base;

import com.agna.ferro.mvp.view.activity.MvpActivityView;
import com.agna.ferro.sample.app.App;
import com.agna.ferro.sample.app.AppComponent;

/**
 * Base class for view, based on Activity
 */
public abstract class BaseActivityView extends MvpActivityView {

    protected AppComponent getAppComponent() {
        return ((App) getApplication()).getAppComponent();
    }
}
