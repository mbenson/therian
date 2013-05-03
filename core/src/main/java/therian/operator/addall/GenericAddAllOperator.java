package therian.operator.addall;

import java.lang.reflect.Type;

import therian.Operator;
import therian.TherianContext;
import therian.buildweaver.StandardOperator;
import therian.operation.Add;
import therian.operation.AddAll;
import therian.operation.Convert;
import therian.operation.GetElementType;
import therian.util.Positions;

@StandardOperator
public class GenericAddAllOperator implements Operator<AddAll<?, ?>> {

    @Override
    public boolean perform(TherianContext context, AddAll<?, ?> operation) {
        final Type sourceElementType =
            context.evalIfSupported(GetElementType.of(operation.getSourcePosition()), operation.getSourcePosition()
                .getType());

        for (Object o : context.eval(Convert.to(Iterable.class, operation.getSourcePosition()))) {
            if (!context.evalSuccess(Add.to(operation.getTargetPosition(), Positions.readOnly(sourceElementType, o)))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean supports(TherianContext context, AddAll<?, ?> operation) {
        final Type sourceElementType =
            context.evalIfSupported(GetElementType.of(operation.getSourcePosition()), operation.getSourcePosition()
                .getType());

        @SuppressWarnings("rawtypes")
        final Convert<?, Iterable> toIterable = Convert.to(Iterable.class, operation.getSourcePosition());

        if (!context.supports(toIterable)) {
            return false;
        }

        for (Object o : context.eval(toIterable)) {
            if (!context.supports(Add.to(operation.getTargetPosition(), Positions.readOnly(sourceElementType, o)))) {
                return false;
            }
        }
        return true;
    }

}
