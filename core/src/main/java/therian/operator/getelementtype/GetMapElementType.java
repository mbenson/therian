package therian.operator.getelementtype;

import java.util.Map;

import org.apache.commons.lang3.reflect.TypeUtils;

import therian.Operator;
import therian.TherianContext;
import therian.operation.GetElementType;
import therian.util.Types;

@SuppressWarnings("rawtypes")
// not sure if this should be a standard operator...
// @StandardOperator
public class GetMapElementType implements Operator<GetElementType<Map>> {

    @Override
    public boolean perform(TherianContext context, GetElementType<Map> op) {
        op.setResult(Types.unrollVariables(TypeUtils.getTypeArguments(op.getTypedItem().getType(), Map.class),
            Map.class.getTypeParameters()[1]));
        return true;
    }

    @Override
    public boolean supports(TherianContext context, GetElementType<Map> op) {
        return true;
    }

}
