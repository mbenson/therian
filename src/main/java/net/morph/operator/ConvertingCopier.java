package net.morph.operator;

import net.morph.MorphContext;
import net.morph.Operator;
import net.morph.operation.Convert;
import net.morph.operation.Copy;
import net.morph.operation.ImmutableCheck;
import net.morph.position.Position;

/**
 * {@link Copy} {@link Operator} that attempts overwriting conversion for
 * writable target positions storing immutable values.
 */
public class ConvertingCopier implements Operator<Copy<?, ?>> {

    public void perform(Copy<?, ?> operation) {
        //TODO create convert operation
        Convert<?, ?> convert = null;
        MorphContext.getRequiredInstance().delegateSuccess(convert);
    }

    public boolean supports(Copy<?, ?> operation) {
        return operation.getTargetPosition() instanceof Position.Writable
            && MorphContext.getRequiredInstance().perform(ImmutableCheck.of(operation.getTargetPosition().getValue()))
                .booleanValue();
    }

}
