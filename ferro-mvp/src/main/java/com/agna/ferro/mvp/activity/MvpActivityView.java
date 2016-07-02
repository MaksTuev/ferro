/*
 * Copyright 2015 Maxim Tuev.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.agna.ferro.mvp.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;

import com.agna.ferro.core.PSSActivity;
import com.agna.ferro.core.PersistentScreenScope;
import com.agna.ferro.mvp.BaseView;
import com.agna.ferro.mvp.dagger.ScreenComponent;
import com.agna.ferro.mvp.dagger.module.MvpActivityModule;
import com.agna.ferro.mvp.dagger.module.MvpFragmentV4Module;
import com.agna.ferro.mvp.dagger.provider.ActivityProvider;
import com.agna.ferro.mvp.dagger.provider.FragmentProvider;
import com.agna.ferro.mvp.presenter.MvpPresenter;


/**
 * Base class for view, based on {@link PSSActivity}.
 *
 * This view save dagger component of the screen in {@link PersistentScreenScope} and reused it,
 * when configuration changed. {@link MvpPresenter} also would be saved, because it is part of
 * dagger component (presenter must be injected in view via dagger mechanism).
 * Dagger component will be destroyed when the screen is finally destroyed,
 * see {@link PersistentScreenScope} life cycle.
 *
 * Objects, provided by dagger component must not contains direct link to Activity, Fragment etc.
 * But if you want to has access to it, you can use {@link ActivityProvider} or
 * {@link FragmentProvider}, which provided by {@link MvpActivityModule} and
 * {@link MvpFragmentV4Module} respectively.
 */
public abstract class MvpActivityView extends PSSActivity implements BaseView {

    private Handler handler = new Handler();

    /**
     * @return layout resource of the screen
     */
    @LayoutRes
    protected abstract int getContentView();

    /**
     * @return dagger component of the screen
     */
    protected abstract ScreenComponent createScreenComponent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentView());
        satisfyDependencies();
        bindPresenter();
        getPresenter().onLoad(isScreenRecreated());
        handler.post(new Runnable() {
            @Override
            public void run() {
                getPresenter().onLoadFinished();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        getPresenter().onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        getPresenter().onStop();
    }

    /**
     * Satisfy dagger dependencies
     */
    protected void satisfyDependencies() {
        PersistentScreenScope screenScope = getPersistentScreenScope();
        assert screenScope != null;
        ScreenComponent component = getScreenComponent();
        if (component == null) {
            component = createScreenComponent();
            screenScope.putObject(component, ScreenComponent.class);
        }
        component.inject(this);
    }

    @Nullable
    private ScreenComponent getScreenComponent() {
        PersistentScreenScope screenScope = getPersistentScreenScope();
        return screenScope.getObject(ScreenComponent.class);
    }

    /**
     * Bind presenter to this view
     */
    @Override
    public final void bindPresenter() {
        getPresenter().attachView(this);
        getPersistentScreenScope().addOnScopeDestroyListener(getPresenter().getOnScopeDestroyListener());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getPresenter().detachView();
    }

}
