package therian.operator.convert;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Collection;

import org.apache.commons.lang3.reflect.TypeUtils;

import therian.Operator;
import therian.TherianContext;
import therian.operation.Convert;
import therian.position.Position;
import therian.util.Types;

@SuppressWarnings("rawtypes")
public class CollectionToArray implements Operator<Convert<? extends Collection, ?>> {

    @SuppressWarnings("unchecked")
    public void perform(TherianContext context, Convert<? extends Collection, ?> convert) {
        final Type targetComponentType = TypeUtils.getArrayComponentType(convert.getTargetPosition().getType());
        ((Position.Writable) convert.getTargetPosition()).setValue(convert
            .getSourcePosition()
            .getValue()
            .toArray(
                (Object[]) Array.newInstance(TypeUtils.getRawType(targetComponentType, null), convert
                    .getSourcePosition().getValue().size())));
        convert.setSuccessful(true);
    }

    public boolean supports(TherianContext context, Convert<? extends Collection, ?> convert) {
        if (!TypeUtils.isAssignable(convert.getSourcePosition().getType(), Collection.class)) {
            return false;
        }
        final Type targetComponentType = TypeUtils.getArrayComponentType(convert.getTargetPosition().getType());
        if (targetComponentType == null) {
            return false;
        }
        final Type sourceElementType =
            Types.unrollVariables(TypeUtils.getTypeArguments(convert.getSourcePosition().getType(), Collection.class),
                Collection.class.getTypeParameters()[0]);

        return TypeUtils.isAssignable(sourceElementType, targetComponentType);
    }

}
