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
package com.agna.ferro.core;

import android.os.Bundle;
import androidx.fragment.app.Fragment;


/**
 * This fragment provide access to own {@link PersistentScreenScope}
 * PSS - PersistentScreenScope
 * <p>
 * The name from {@link HasName#getName()} used for distinguish one PersistentScreenScope from
 * another inside one Activity. You can use this name for logging, analytics etc.
 */
public abstract class PSSFragmentV4 extends Fragment implements PSSParentScreen {

    private PSSDelegate delegate = new PSSFragmentDelegate(this, this);

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        delegate.init();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        delegate.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        delegate.checkUniqueScreenName();
    }

    /**
     * @return true if screen was recreated after changing configuration
     */
    public boolean isScreenRecreated() {
        return delegate.isScreenRecreated();
    }

    /**
     * @return {@link PersistentScreenScope} of this screen
     */
    @Override
    public PersistentScreenScope getPersistentScreenScope() {
        return delegate.getScreenScope();
    }

    /**
     * Clear all objects from own {@link PersistentScreenScope}
     */
    @Deprecated
    public void clearPersistentScreenScope() {
        PersistentScreenScope screenScope = delegate.getScreenScope();
        if (screenScope != null) {
            screenScope.clear();
        }
    }
}
