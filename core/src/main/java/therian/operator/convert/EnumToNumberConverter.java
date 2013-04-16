package therian.operator.convert;

import java.lang.reflect.Type;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.TypeUtils;

import therian.Operator;
import therian.TherianContext;
import therian.operation.Convert;
import therian.position.Ref;

/**
 * Convert an enum value to any number, by way of {@link Enum#ordinal()}. Because multiple target types are served (by
 * delegation), the destination parameter is unspecified at the class level.
 */
public class EnumToNumberConverter implements Operator<Convert<Enum<?>, ?>> {

    public void perform(TherianContext context, Convert<Enum<?>, ?> operation) {
        context.forwardTo(Convert.to(operation.getTargetPosition(),
            Ref.to(operation.getSourcePosition().getValue().ordinal())));
    }

    public boolean supports(TherianContext context, Convert<Enum<?>, ?> operation) {
        if (!TypeUtils.isAssignable(operation.getSourcePosition().getType(), Enum.class)) {
            return false;
        }
        final Type targetType = operation.getTargetPosition().getType();
        if (targetType instanceof Class == false) {
            return false;
        }
        final Class<?> targetClass = (Class<?>) targetType;
        return Number.class.isAssignableFrom(targetClass) && ClassUtils.wrapperToPrimitive(targetClass) != null;
    }

}
