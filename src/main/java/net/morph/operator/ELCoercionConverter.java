package net.morph.operator;

import javax.el.ELException;
import javax.el.ExpressionFactory;

import org.apache.commons.lang3.reflect.TypeUtils;

import net.morph.MorphContext;
import net.morph.Operator;
import net.morph.operation.Convert;

/**
 * Special operator that handles conversions by applying EL coercion rules.
 * Quintessential example of a "converter" that does not extend Converter due to
 * its differing type allowances. Intended as a fallback strategy to implement
 * "simple" conversions (aka coercions) when other approaches have been
 * exhausted.
 */
public class ELCoercionConverter implements Operator<Convert<?, ?>> {

    public void perform(Convert<?, ?> operation) {
        final Object value;
        try {
            value =
                MorphContext.getRequiredInstance().getTypedContext(ExpressionFactory.class)
                    .coerceToType(operation.getSourcePosition().getValue(), getRawTargetType(operation));
        } catch (ELException e) {
            return;
        }
        @SuppressWarnings("unchecked")
        final Convert<?, Object> raw = (Convert<?, Object>) operation;
        raw.getTargetPosition().setValue(value);
        operation.setSuccessful(true);
    }

    public boolean supports(Convert<?, ?> operation) {
        final Class<?> rawTargetType = getRawTargetType(operation);
        // TODO check for actual cases enumerated by the EL spec
        return rawTargetType != null;
    }

    private static Class<?> getRawTargetType(Convert<?, ?> operation) {
        // TODO is this enough?
        return TypeUtils.getRawType(operation.getTargetPosition().getType(), null);
    }
}