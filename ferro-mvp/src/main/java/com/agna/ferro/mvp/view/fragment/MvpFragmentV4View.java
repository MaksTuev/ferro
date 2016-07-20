/*
 * Copyright 2016 Maxim Tuev.
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
package com.agna.ferro.mvp.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.agna.ferro.core.PSSFragmentV4;
import com.agna.ferro.core.PersistentScreenScope;
import com.agna.ferro.mvp.view.BaseView;
import com.agna.ferro.mvp.component.ScreenComponent;
import com.agna.ferro.mvp.component.provider.ActivityProvider;
import com.agna.ferro.mvp.component.provider.FragmentProvider;
import com.agna.ferro.mvp.presenter.MvpPresenter;


/**
 * Base class for view, based on {@link PSSFragmentV4}.
 * <p>
 * This view save its {@link ScreenComponent} in {@link PersistentScreenScope} and reused it,
 * when configuration changed. Screen's {@link MvpPresenter} must be part of ScreenComponent and
 * must be inserted in this view in method {@link ScreenComponent#inject(BaseView)}.
 * The simplest way is when ScreenComponent is dagger component.
 *
 * ScreenComponent will be destroyed when the screen is finally destroyed,
 * see {@link PersistentScreenScope} life cycle.
 * <p>
 * Objects (besides presenter), provided by ScreenComponent must not contains direct link to
 * Activity, Fragment etc. But if you want to has access to it, you can use {@link ActivityProvider}
 * or {@link FragmentProvider}.
 *
 * The name from {@link BaseView#getName()} used for distinguish one {@link PersistentScreenScope}
 * from another inside one Activity. You can use this name for logging, analytics etc.
 */
public abstract class MvpFragmentV4View extends PSSFragmentV4 implements BaseView {

    /**
     * @return unique screen name
     */
    public abstract String getName();

    /**
     * @return screen component
     */
    protected abstract ScreenComponent createScreenComponent();

    @Override
    public final void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        satisfyDependencies();
        bindPresenter();
        onActivityCreated(savedInstanceState, isScreenRecreated());
        getPresenter().onLoad(isScreenRecreated());
        getPresenter().onLoadFinished();
    }

    /**
     * Override this instead {@link #onActivityCreated(Bundle)}
     * @param viewRecreated show whether view created in first time or recreated after
     *                        changing configuration
     */
    public void onActivityCreated(Bundle savedInstanceState, boolean viewRecreated) {

    }

    /**
     * Bind presenter to this view
     */
    @Override
    public final void bindPresenter() {
        getPresenter().attachView(this);
        if(!isScreenRecreated()) {
            getPersistentScreenScope().addOnScopeDestroyListener(getPresenter());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getPresenter().onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPresenter().onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        getPresenter().onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        getPresenter().onStop();
    }

    /**
     * Satisfy dependencies
     */
    protected void satisfyDependencies() {
        PersistentScreenScope screenScope = getPersistentScreenScope();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getPresenter().detachView();
    }

}
