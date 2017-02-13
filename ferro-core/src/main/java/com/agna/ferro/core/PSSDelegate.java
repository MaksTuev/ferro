package com.agna.ferro.core;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

/**
 * base delegate for managing {@link PersistentScreenScope}
 */
public abstract class PSSDelegate {

    private PersistentScreenScope screenScope;
    private HasName screenNameProvider;

    public PSSDelegate(HasName screenNameProvider) {
        this.screenNameProvider = screenNameProvider;
    }

    protected abstract FragmentActivity getParentActivity();

    protected abstract Fragment getParentFragment();

    protected PersistentScreenScope createPersistentScreenScope() {
        return new PersistentScreenScope();
    }

    public PersistentScreenScope getScreenScope() {
        return screenScope;
    }

    public boolean isScreenRecreated() {
        return screenScope.isScreenRecreated();
    }

    public void init() {
        screenScope = PersistentScreenScope.find(getFragmentManager(), screenNameProvider.getName());
        if (screenScope == null) {
            screenScope = createPersistentScreenScope();
            screenScope.attach(getFragmentManager(), screenNameProvider.getName());
        }
        screenScope.setParentActivity(getParentActivity());
        screenScope.setParentPSSFragment(getParentFragment());
    }

    private FragmentManager getFragmentManager() {
        return getParentActivity().getSupportFragmentManager();
    }

    public void onDestroy() {
        if (screenScope != null) {
            screenScope.clearParentActivity();
            screenScope.clearParentPSSFragment();
        }
    }

    public void checkUniqueScreenName() {
        checkUniqueScreenName(getParentActivity());
        for(Fragment fragment : getFragmentManager().getFragments()){
            checkUniqueScreenName(fragment);
        }
    }

    private void checkUniqueScreenName(Object anotherScreen) {
        if (screenNameProvider != anotherScreen) {
            if(HasName.class.isInstance(anotherScreen)){
                String anotherName = ((PSSParentScreen)anotherScreen).getName();
                if(anotherName.equals(screenNameProvider.getName())){
                    throw new IllegalStateException("two screens has same name: "+ anotherScreen
                            + ", " + screenNameProvider);
                }
            }
        }
    }
}
