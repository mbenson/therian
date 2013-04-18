package therian.operator.getelementtype;

import java.util.Enumeration;

import org.apache.commons.lang3.reflect.TypeUtils;

import therian.Operator;
import therian.TherianContext;
import therian.operation.GetElementType;
import therian.util.Types;

@SuppressWarnings("rawtypes")
public class GetEnumerationElementType implements Operator<GetElementType<Enumeration>> {

    public void perform(TherianContext context, GetElementType<Enumeration> op) {
        op.setResult(Types.unrollVariables(TypeUtils.getTypeArguments(op.getTypeHost().getType(), Enumeration.class),
            Enumeration.class.getTypeParameters()[0]));
        op.setSuccessful(true);
    }

    public boolean supports(TherianContext context, GetElementType<Enumeration> op) {
        return true;
    }

}
