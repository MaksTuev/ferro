package com.agna.ferro.core;

/**
 * interface for screen, which is parent of {@link PersistentScreenScope}
 */
public interface PSSParentScreen extends HasName {

    /**
     * @return {@link PersistentScreenScope} of this screen
     */
    PersistentScreenScope getPersistentScreenScope();

}
