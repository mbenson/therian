package therian.operator.getelementtype;

import org.apache.commons.lang3.reflect.TypeUtils;

import therian.Operator;
import therian.TherianContext;
import therian.buildweaver.StandardOperator;
import therian.operation.GetElementType;

@StandardOperator
public class GetArrayElementType implements Operator<GetElementType<Object>> {

    @Override
    public boolean perform(TherianContext context, GetElementType<Object> op) {
        op.setResult(TypeUtils.getArrayComponentType(op.getTypedItem().getType()));
        return true;
    }

    @Override
    public boolean supports(TherianContext context, GetElementType<Object> op) {
        return TypeUtils.isArrayType(op.getTypedItem().getType());
    }

}
