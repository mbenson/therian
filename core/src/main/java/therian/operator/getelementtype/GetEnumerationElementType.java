package therian.operator.getelementtype;

import java.util.Enumeration;

import org.apache.commons.lang3.reflect.TypeUtils;

import therian.Operator;
import therian.TherianContext;
import therian.buildweaver.StandardOperator;
import therian.operation.GetElementType;
import therian.util.Types;

@SuppressWarnings("rawtypes")
@StandardOperator
public class GetEnumerationElementType implements Operator<GetElementType<Enumeration>> {

    @Override
    public boolean perform(TherianContext context, GetElementType<Enumeration> op) {
        op.setResult(Types.unrollVariables(TypeUtils.getTypeArguments(op.getTypedItem().getType(), Enumeration.class),
            Enumeration.class.getTypeParameters()[0]));
        return true;
    }

    @Override
    public boolean supports(TherianContext context, GetElementType<Enumeration> op) {
        return true;
    }

}
