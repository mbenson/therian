package therian.operator;

import therian.Operation;
import therian.TherianContext;

public abstract class OptimisticOperatorBase<T extends Operation<?>> extends OperatorBase<T> {

    @Override
    public final boolean supports(TherianContext context, T operation) {
        return true;
    }
}
