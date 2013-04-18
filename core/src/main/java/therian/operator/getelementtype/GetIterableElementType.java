package therian.operator.getelementtype;

import org.apache.commons.lang3.reflect.TypeUtils;

import therian.Operator;
import therian.TherianContext;
import therian.operation.GetElementType;
import therian.util.Types;

@SuppressWarnings("rawtypes")
public class GetIterableElementType implements Operator<GetElementType<Iterable>> {

    public boolean perform(TherianContext context, GetElementType<Iterable> op) {
        op.setResult(Types.unrollVariables(TypeUtils.getTypeArguments(op.getTypeHost().getType(), Iterable.class),
            Iterable.class.getTypeParameters()[0]));
        return true;
    }

    public boolean supports(TherianContext context, GetElementType<Iterable> op) {
        return true;
    }

}
