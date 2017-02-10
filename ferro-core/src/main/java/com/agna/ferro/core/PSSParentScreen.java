package com.agna.ferro.core;

public interface PSSParentScreen extends HasName {
    /**
     * @return true if screen was recreated after changing configuration
     */
    boolean isScreenRecreated();

    /**
     * @return {@link PersistentScreenScope} of this screen
     */
    PersistentScreenScope getPersistentScreenScope();

}
