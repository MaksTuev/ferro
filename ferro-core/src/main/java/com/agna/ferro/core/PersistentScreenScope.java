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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * This class is storage of objects, which not destroyed if configuration changed.
 * PersistentScreenScope created for each screen, based on {@link PSSActivity} or
 * {@link PSSFragmentV4}.
 *
 * This storage destroyed only if parent screen is finally destroyed (e.g. after Activity.finish()).
 * If you manually destroy screen, based on {@link PSSFragmentV4} via FragmentManager, and you want
 * immediately destroy PersistentScreenScope need call {@link #destroy()} or
 * {@link PersistentScreenScope#destroy(AppCompatActivity, String)},
 * otherwise PersistentScreenScope to be destroyed after the root activity is finally destroyed.
 * In reality, PersistentScreenScope is retained fragment without view.
 */
public class PersistentScreenScope extends Fragment {

    private static final String SCREEN_SCOPE_NAME_FORMAT = "screen_scope_%s";

    private final Set<OnScopeDestroyListener> onScopeDestroyListeners = new HashSet<>();
    private final Map<ObjectKey, Object> objects = new HashMap<>();
    private boolean destroyed = false;
    private boolean screenRecreated = false;

    /**
     * Destroy {@link PersistentScreenScope}
     * This method need to use only if you manually destroy screen based on {@link PSSFragmentV4},
     * and you want immediately destroy PersistentScreenScope
     *
     * @param rootActivity - activity, which contains destroyed screen
     * @param screenName   name of screen, see {@link PSSFragmentV4#getName()}
     */
    public static void destroy(AppCompatActivity rootActivity, String screenName) {
        PersistentScreenScope persistentScreenScope =
                (PersistentScreenScope) rootActivity.getSupportFragmentManager()
                        .findFragmentByTag(getName(screenName));
        if (persistentScreenScope != null) {
            persistentScreenScope.destroy();
        }
    }

    /**
     * @return PersistentScreenScope for parentActivity or null, if it not exist
     */
    @Nullable
    static PersistentScreenScope find(PSSActivity parentActivity){
        return (PersistentScreenScope) parentActivity.getSupportFragmentManager()
                .findFragmentByTag(getName(parentActivity.getName()));
    }

    /**
     * @return PersistentScreenScope for parentFragment or null, if it not exist
     */
    @Nullable
    static PersistentScreenScope find(PSSFragmentV4 parentFragment){
        return (PersistentScreenScope) parentFragment.getFragmentManager()
                .findFragmentByTag(getName(parentFragment.getName()));
    }

    /**
     * @param screenName - name of screen, returned from {@link PSSActivity#getName()} or
     *                   {@link PSSFragmentV4#getName()}
     * @return name of  {@link PersistentScreenScope}
     */
    private static String getName(String screenName) {
        return String.format(SCREEN_SCOPE_NAME_FORMAT, screenName);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyed = true;
        for (OnScopeDestroyListener onDestroyListener : onScopeDestroyListeners) {
            onDestroyListener.onDestroy();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        screenRecreated = true;
    }

    public boolean isScreenRecreated(){
        return screenRecreated;
    }

    /**
     * Put object to scope
     *
     * @param object
     * @param tag    - key, which used for store object in scope
     */
    public <T> void putObject(T object, String tag) {
        assertNotDestroyed();
        ObjectKey key = new ObjectKey(tag);
        objects.put(key, object);
    }

    /**
     * Put object to scope
     *
     * @param object
     * @param clazz  key, which used for store object in scope
     */
    public <T> void putObject(T object, Class<T> clazz) {
        assertNotDestroyed();
        ObjectKey key = new ObjectKey(clazz);
        objects.put(key, object);
    }

    /**
     * @param tag key
     * @return object from scope
     */
    @Nullable
    public <T> T getObject(String tag) {
        assertNotDestroyed();
        ObjectKey key = new ObjectKey(tag);
        return (T)objects.get(key);
    }

    /**
     * @param clazz key
     * @return object from scope
     */
    @Nullable
    public <T> T getObject(Class<T> clazz) {
        assertNotDestroyed();
        ObjectKey key = new ObjectKey(clazz);
        return clazz.cast(objects.get(key));
    }

    /**
     * Remove all objects from scope
     */
    public void clear() {
        objects.clear();
    }

    /**
     * Register a callback to be invoked when this scope is destroyed.
     */
    public void addOnScopeDestroyListener(OnScopeDestroyListener onScopeDestroyListener) {
        assert onScopeDestroyListener != null;
        this.onScopeDestroyListeners.add(onScopeDestroyListener);
    }

    /**
     * Unregister a callback to be invoked when this scope is destroyed.
     */
    public void removeOnScopeDestroyListener(OnScopeDestroyListener onScopeDestroyListener) {
        this.onScopeDestroyListeners.remove(onScopeDestroyListener);
    }

    /**
     * Bind this PersistentScreenScope to parentActivity
     * @param parentActivity
     */
    void attach(PSSActivity parentActivity){
        FragmentTransaction ft = parentActivity.getSupportFragmentManager().beginTransaction();
        ft.add(this, PersistentScreenScope.getName(parentActivity.getName()));
        ft.commit();
    }

    /**
     * Bind this PersistentScreenScope to parentFragment
     * @param parentFragment
     */
    void attach(PSSFragmentV4 parentFragment){
        this.setTargetFragment(parentFragment, 0);
        FragmentTransaction ft = parentFragment.getFragmentManager().beginTransaction();
        ft.add(this, PersistentScreenScope.getName(parentFragment.getName()));
        ft.commit();
    }


    /**
     * Destroy scope
     * This method need to use only if you manually destroy screen based on {@link PSSFragmentV4},
     * and you want immediately destroy PersistentScreenScope
     */
    public void destroy() {
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager()
                .beginTransaction();
        fragmentTransaction.remove(this);
        fragmentTransaction.commit();
    }

    private void assertNotDestroyed() {
        if (destroyed) {
            throw new IllegalStateException("Unsupported operation, PersistentScreenScope is destroyed");
        }
    }

    public interface OnScopeDestroyListener {
        void onDestroy();
    }
}
