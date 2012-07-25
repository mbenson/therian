package net.morph.operator;

import net.morph.Operator;
import net.morph.operation.ImmutableCheck;

/**
 * {@link ImmutableCheck} {@link Operator}.
 */
public abstract class ImmutableChecker implements Operator<ImmutableCheck> {
    public final void perform(ImmutableCheck immutableCheck) {
        immutableCheck.setSuccessful(isImmutable(immutableCheck.getObject()));
    }

    public boolean supports(ImmutableCheck immutableCheck) {
        return true;
    }

    protected abstract boolean isImmutable(Object object);
}
