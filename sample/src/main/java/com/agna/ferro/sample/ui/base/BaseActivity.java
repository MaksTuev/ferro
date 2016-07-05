package com.agna.ferro.sample.ui.base;

import android.support.v7.app.AppCompatActivity;

import com.agna.ferro.sample.app.App;
import com.agna.ferro.sample.app.AppComponent;

/**
 * Base class for Activity, which used how container for fragment
 */
public abstract class BaseActivity extends AppCompatActivity {

    public AppComponent getAppComponent() {
        return ((App) getApplication()).getAppComponent();
    }
}
