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
package com.agna.ferro.mvp.component.provider;


import android.support.v4.app.Fragment;

import com.agna.ferro.core.PersistentScreenScope;

/**
 * Provider for Fragment
 * every call {@link this#get()} return actual Fragment
 */
public class FragmentProvider {
    private PersistentScreenScope screenScope;

    public FragmentProvider(PersistentScreenScope screenScope) {
        this.screenScope = screenScope;
    }

    /**
     * @return actual Fragment
     */
    public Fragment get() {
        return screenScope.getTargetFragment();
    }
}
