package com.agna.ferro.core;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

/**
 * delegate for managing of {@link PersistentScreenScope} on activity
 */
public class PSSActivityDelegate extends PSSDelegate {

    private final FragmentActivity parentActivity;

    public PSSActivityDelegate(HasName screenNameProvider, FragmentActivity parentActivity) {
        super(screenNameProvider);
        this.parentActivity = parentActivity;
    }

    @Override
    protected FragmentActivity getParentActivity() {
        return parentActivity;
    }

    @Override
    protected Fragment getParentFragment() {
        return null;
    }
}
