package therian.operator.convert;

import org.apache.commons.lang3.reflect.TypeUtils;

import therian.TherianContext;
import therian.operation.Convert;
import therian.operation.ImmutableCheck;
import therian.position.Position;

/**
 * Uses source value as target value when assignable and immutable.
 */
public class NOPConverter implements therian.Operator<Convert<?, ?>> {

    public void perform(Convert<?, ?> operation) {
        // silly anal ways to avoid suppressing warnings on the whole method:
        @SuppressWarnings("rawtypes")
        final Convert raw = operation;
        @SuppressWarnings({ "unused", "unchecked" })
        final Void dummy = dumpTo(raw.getTargetPosition(), raw.getSourcePosition());
        operation.setSuccessful(true);
    }

    private <T> Void dumpTo(Position.Writable<? super T> target, Position.Readable<? extends T> source) {
        target.setValue(source.getValue());
        return null;
    }

    public boolean supports(Convert<?, ?> operation) {
        return TypeUtils.isAssignable(operation.getSourcePosition().getType(), operation.getTargetPosition().getType())
            && TherianContext.getInstance().eval(ImmutableCheck.of(operation.getSourcePosition())).booleanValue();
    }

}
