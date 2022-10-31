package com.agna.ferro.core;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

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
