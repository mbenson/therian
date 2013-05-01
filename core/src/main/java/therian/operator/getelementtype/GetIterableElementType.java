package therian.operator.getelementtype;

import org.apache.commons.lang3.reflect.TypeUtils;

import therian.Operator;
import therian.TherianContext;
import therian.buildweaver.StandardOperator;
import therian.operation.GetElementType;
import therian.util.Types;

@SuppressWarnings("rawtypes")
@StandardOperator
public class GetIterableElementType implements Operator<GetElementType<Iterable>> {

    @Override
    public boolean perform(TherianContext context, GetElementType<Iterable> op) {
        op.setResult(Types.unrollVariables(TypeUtils.getTypeArguments(op.getTypedItem().getType(), Iterable.class),
            Iterable.class.getTypeParameters()[0]));
        return true;
    }

    @Override
    public boolean supports(TherianContext context, GetElementType<Iterable> op) {
        return true;
    }

}
