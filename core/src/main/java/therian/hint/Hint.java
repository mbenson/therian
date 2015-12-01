package therian.hint;

import therian.Operator;
import therian.TherianContext;

/**
 * Generalizes a hint targeted to some {@link Operator} that can be set on the context. Note that a Hint should
 * properly implement {@link #equals(Object)} and {@link #hashCode()}.
 *
 * @see TherianContext#doWithHints(Job, Hint...)
 */
public interface Hint {

    /**
     * Get the hint type to use.
     *
     * @return Class
     */
    Class<? extends Hint> getType();
}