package therian.operator.getelementtype;

import java.util.Iterator;

import org.apache.commons.lang3.reflect.TypeUtils;

import therian.Operator;
import therian.TherianContext;
import therian.buildweaver.StandardOperator;
import therian.operation.GetElementType;
import therian.util.Types;

@SuppressWarnings("rawtypes")
@StandardOperator
public class GetIteratorElementType implements Operator<GetElementType<Iterator>> {

    @Override
    public boolean perform(TherianContext context, GetElementType<Iterator> op) {
        op.setResult(Types.unrollVariables(TypeUtils.getTypeArguments(op.getTypedItem().getType(), Iterator.class),
            Iterator.class.getTypeParameters()[0]));
        return true;
    }

    @Override
    public boolean supports(TherianContext context, GetElementType<Iterator> op) {
        return true;
    }

}
