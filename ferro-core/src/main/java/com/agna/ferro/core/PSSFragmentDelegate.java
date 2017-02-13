package com.agna.ferro.core;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

/**
 * delegate for managing of {@link PersistentScreenScope} on fragment
 */
public class PSSFragmentDelegate extends PSSDelegate {

    private final Fragment parentFragment;

    public PSSFragmentDelegate(HasName screenNameProvider, Fragment parentFragment) {
        super(screenNameProvider);
        this.parentFragment = parentFragment;
    }

    @Override
    protected FragmentActivity getParentActivity() {
        return parentFragment.getActivity();
    }

    @Override
    protected Fragment getParentFragment() {
        return parentFragment;
    }
}
