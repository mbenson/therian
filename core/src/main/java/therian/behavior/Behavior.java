package therian.behavior;

import therian.Therian;

/**
 * Describes a behavior applicable to a {@link Therian} instance.
 */
public interface Behavior {

    /**
     * Get the behavior type to use.
     *
     * @return Class
     */
    Class<? extends Behavior> getType();
}