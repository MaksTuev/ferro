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
package com.agna.ferro.mvp.presenter;

import com.agna.ferro.core.PersistentScreenScope;
import com.agna.ferro.mvp.view.BaseView;
import com.agna.ferro.mvp.component.ScreenComponent;

/**
 * Base class for presenter.
 * When configuration changed, presenter isn't destroyed and reused for new view
 * <p>
 * Presenter must be injected in view in method {@link ScreenComponent#inject(BaseView)}.
 * It contains methods corresponding to the screen life cycle.
 *
 * @param <V> - View type
 */
public class MvpPresenter<V extends BaseView> implements
        PersistentScreenScope.OnScopeDestroyListener {

    private V view;

    public void attachView(V view) {
        this.view = view;
    }

    /**
     * @return screen's view
     */
    protected V getView() {
        return view;
    }

    /**
     * This method is called, when view is ready
     * @param viewRecreated - show whether view created in first time or recreated after
     *                        changing configuration
     */
    public void onLoad(boolean viewRecreated) {
    }

    /**
     * Called after {@link this#onLoad}
     */
    public void onLoadFinished() {

    }

    /**
     * Called when view is started
     */
    public void onStart(){

    }

    /**
     * Called when view is resumed
     */
    public void onResume(){

    }

    /**
     * Called when view is paused
     */
    public void onPause(){

    }

    /**
     * Called when view is stopped
     */
    public void onStop(){

    }

    public final void detachView() {
        view = null;
        onViewDetached();
    }

    /**
     * Called when view is detached
     */
    protected void onViewDetached() {

    }

    /**
     * Called when screen is finally destroyed
     */
    public void onDestroy() {

    }
}
