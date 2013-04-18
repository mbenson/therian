package therian.operator.getelementtype;

import org.apache.commons.lang3.reflect.TypeUtils;

import therian.Operator;
import therian.TherianContext;
import therian.operation.GetElementType;

public class GetArrayElementType implements Operator<GetElementType<Object>> {

    public boolean perform(TherianContext context, GetElementType<Object> op) {
        op.setResult(TypeUtils.getArrayComponentType(op.getTypeHost().getType()));
        return true;
    }

    public boolean supports(TherianContext context, GetElementType<Object> op) {
        return TypeUtils.isArrayType(op.getTypeHost().getType());
    }

}
