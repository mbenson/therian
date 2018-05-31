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
package therian.operator.copy;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.reflect.TypeUtils;

import therian.Operator.DependsOn;
import therian.TherianContext;
import therian.buildweaver.StandardOperator;
import therian.operation.Convert;
import therian.operation.Copy;
import therian.operator.convert.ELCoercionConverter;
import therian.operator.convert.NOPConverter;
import therian.position.Position;
import therian.position.relative.Keyed;
import therian.position.relative.Property;
import therian.util.BeanProperties;
import therian.util.BeanProperties.ReturnProperties;
import therian.util.Positions;

/**
 * Copy a {@link Map}'s properties onto a bean.
 */
@SuppressWarnings("rawtypes")
@StandardOperator
@DependsOn({ NOPConverter.class, ConvertingCopier.class, ELCoercionConverter.class })
public class MapToBeanCopier extends Copier<Map, Object> {
    public static final String[] IGNORED_PROPERTIES = { "class" };

    private static Type getKeyType(Position<? extends Map> target) {
        return ObjectUtils.defaultIfNull(
            TypeUtils.unrollVariables(TypeUtils.getTypeArguments(target.getType(), Map.class),
                Map.class.getTypeParameters()[0]), Object.class);
    }

    @Override
    public boolean perform(TherianContext context, Copy<? extends Map, ? extends Object> copy) {
        final Type sourceKeyType = getKeyType(copy.getSourcePosition());

        final Set<String> propertyNames =
            BeanProperties.getPropertyNames(ReturnProperties.WRITABLE, context, copy.getTargetPosition());

        boolean result = false;

        final Set<?> keys = copy.getSourcePosition().getValue().keySet();
        for (Object key : keys) {
            final String propertyName = context.eval(Convert.to(String.class, Positions.readOnly(sourceKeyType, key)));
            if (propertyNames.contains(propertyName)) {
                @SuppressWarnings("unchecked")
                final Position.Readable<?> source = Keyed.value().at(key).of(copy.getSourcePosition());
                final Position.Readable<?> target = Property.at(propertyName).of(copy.getTargetPosition());
                if (context.evalSuccess(Copy.to(target, source))) {
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @return whether at least one entry can be copied
     */
    @Override
    public boolean supports(TherianContext context, Copy<? extends Map, ? extends Object> copy) {
        if (!super.supports(context, copy)) {
            return false;
        }
        final Type sourceKeyType = getKeyType(copy.getSourcePosition());

        final Set<String> propertyNames =
            BeanProperties.getPropertyNames(ReturnProperties.WRITABLE, context, copy.getTargetPosition());

        final Set<?> keys = copy.getSourcePosition().getValue().keySet();
        for (Object key : keys) {
            final String propertyName = context.eval(Convert.to(String.class, Positions.readOnly(sourceKeyType, key)));
            if (propertyNames.contains(propertyName)) {
                @SuppressWarnings("unchecked")
                final Position.Readable<?> source = Keyed.value().at(key).of(copy.getSourcePosition());
                final Position.Readable<?> target = Property.at(propertyName).of(copy.getTargetPosition());
                if (context.supports(Copy.to(target, source))) {
                    return true;
                }
            }
        }
        return false;
    }
}
