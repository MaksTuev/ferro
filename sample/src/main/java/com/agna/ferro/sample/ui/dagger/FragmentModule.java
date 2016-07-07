package com.agna.ferro.sample.ui.dagger;

import com.agna.ferro.core.PersistentScreenScope;
import com.agna.ferro.mvp.component.provider.ActivityProvider;
import com.agna.ferro.mvp.component.provider.FragmentProvider;
import com.agna.ferro.mvp.component.scope.PerScreen;
import com.agna.ferro.mvp.view.fragment.MvpFragmentV4View;

import dagger.Module;
import dagger.Provides;

/**
 * Dagger module, which provide {@link ActivityProvider} and {@link FragmentProvider}
 * used for screens, based on {@link MvpFragmentV4View}
 */
@Module
public class FragmentModule {

    private PersistentScreenScope persistentScreenScope;

    public FragmentModule(PersistentScreenScope persistentScreenScope) {
        this.persistentScreenScope = persistentScreenScope;
    }

    @Provides
    @PerScreen
    ActivityProvider provideActivityProvider() {
        return new ActivityProvider(persistentScreenScope);
    }

    @Provides
    @PerScreen
    FragmentProvider provideFragmentProvider() {
        return new FragmentProvider(persistentScreenScope);
    }
}