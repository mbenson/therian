/*
 *  Copyright the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package therian.operator.convert;

import java.beans.PropertyEditorManager;
import java.math.BigDecimal;
import java.math.BigInteger;

import javax.el.ELException;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.reflect.TypeUtils;

import therian.TherianContext;
import therian.buildweaver.StandardOperator;
import therian.operation.Convert;
import uelbox.UEL;

/**
 * Special operator that handles conversions by applying EL coercion rules. Quintessential example of a "converter" that
 * does not extend Converter due to its differing type allowances. Intended as a fallback strategy to implement "simple"
 * conversions (aka coercions) when other approaches have been exhausted.
 */
// TODO implement a hint that will bypass the null-to-anything rule
@StandardOperator
public class ELCoercionConverter extends Converter.WithDynamicTarget<Object> {
    /**
     * Subclass that does not reject noops. The most apparent effect of using this converter is that {@code null} values
     * will be converted to "default" values per The Expression Language Specification v2.2, section 1.18. As such, this
     * converter is not enabled as a standard operator.
     */
    public static class HandlesNoop extends ELCoercionConverter {
        @Override
        protected boolean isRejectNoop() {
            return false;
        }
    }

    @Override
    public boolean perform(TherianContext context, Convert<?, ?> convert) {
        final Object value;
        try {
            value = UEL.coerceToType(context, getRawTargetType(convert), convert.getSourcePosition().getValue());
        } catch (final ELException e) {
            return false;
        }
        @SuppressWarnings("unchecked")
        final Convert<?, Object> raw = (Convert<?, Object>) convert;
        raw.getTargetPosition().setValue(value);
        return true;
    }

    @Override
    public boolean supports(TherianContext context, Convert<?, ?> convert) {
        if (!super.supports(context, convert)) {
            return false;
        }
        final Class<?> rawTargetType = getRawTargetType(convert);
        final Class<?> useTargetType =
            ObjectUtils.defaultIfNull(ClassUtils.primitiveToWrapper(rawTargetType), rawTargetType);

        // per UEL spec v2.2 section 1.18:
        if (String.class.equals(useTargetType)) {
            return true;
        }
        final Object source = convert.getSourcePosition().getValue();

        if (BigDecimal.class.equals(useTargetType) || BigInteger.class.equals(useTargetType)
            || Number.class.isAssignableFrom(useTargetType) && ClassUtils.wrapperToPrimitive(useTargetType) != null) {
            return source == null || source instanceof String || source instanceof Character
                || source instanceof Number;
        }
        if (Character.class.equals(useTargetType)) {
            return source == null || source instanceof String || source instanceof Number;
        }
        if (Boolean.class.equals(useTargetType)) {
            return source == null || source instanceof String;
        }
        if (Enum.class.isAssignableFrom(useTargetType)) {
            return source == null || source instanceof String;
        }
        return source == null || "".equals(source) || source instanceof String
            && PropertyEditorManager.findEditor(useTargetType) != null;
    }

    private static Class<?> getRawTargetType(Convert<?, ?> operation) {
        return TypeUtils.getRawType(operation.getTargetPosition().getType(), null);
    }
}
