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

import therian.Operator;
import therian.TherianContext;
import therian.operation.Convert;
import therian.uelbox.UEL;

/**
 * Special operator that handles conversions by applying EL coercion rules. Quintessential example of a "converter" that
 * does not extend Converter due to its differing type allowances. Intended as a fallback strategy to implement "simple"
 * conversions (aka coercions) when other approaches have been exhausted.
 */
public class ELCoercionConverter implements Operator<Convert<?, ?>> {

    public void perform(TherianContext context, Convert<?, ?> operation) {
        final Object value;
        try {
            value = UEL.coerceToType(context, getRawTargetType(operation), operation.getSourcePosition().getValue());
        } catch (final ELException e) {
            return;
        }
        @SuppressWarnings("unchecked")
        final Convert<?, Object> raw = (Convert<?, Object>) operation;
        raw.getTargetPosition().setValue(value);
        operation.setSuccessful(true);
    }

    public boolean supports(TherianContext context, Convert<?, ?> operation) {
        final Class<?> rawTargetType = getRawTargetType(operation);
        final Class<?> useTargetType =
            ObjectUtils.defaultIfNull(ClassUtils.primitiveToWrapper(rawTargetType), rawTargetType);

        // per UEL spec v2.2 section 1.18:
        if (String.class.equals(useTargetType)) {
            return true;
        }
        final Object source = operation.getSourcePosition().getValue();

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
