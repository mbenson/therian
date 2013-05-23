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

import org.apache.commons.functor.UnaryPredicate;
import org.apache.commons.functor.core.collection.FilteredIterable;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.reflect.TypeUtils;

import therian.Operator.DependsOn;
import therian.TherianContext;
import therian.operation.Convert;
import therian.operation.Copy;
import therian.operator.convert.NOPConverter;
import therian.position.Position;
import therian.position.relative.Keyed;
import therian.position.relative.Property;
import therian.util.BeanProperties;
import therian.util.Positions;
import therian.util.Types;

/**
 * Copy a bean's properties into a {@link Map}.
 */
@SuppressWarnings("rawtypes")
@DependsOn({ NOPConverter.class, ConvertingCopier.class })
public class BeanToMapCopier extends Copier<Object, Map> {
    public static final String[] IGNORED_PROPERTIES = { "class" };

    private final UnaryPredicate<String> notIgnored = new UnaryPredicate<String>() {

        @Override
        public boolean test(String s) {
            return !isIgnored(s);
        }
    };

    protected boolean isIgnored(String propertyName) {
        return ArrayUtils.contains(IGNORED_PROPERTIES, propertyName);
    }

    @Override
    public boolean perform(TherianContext context, Copy<? extends Object, ? extends Map> copy) {
        final Type targetKeyType = getKeyType(copy.getTargetPosition());

        boolean result = false;
        final Position.ReadWrite<?> targetKey = Positions.readWrite(targetKeyType);
        for (String propertyName : getProperties(context, copy.getSourcePosition())) {
            final Convert<String, ?> convertKey = Convert.to(targetKey, Positions.readOnly(propertyName));
            if (!context.supports(convertKey)) {
                continue;
            }
            final Object key = context.eval(convertKey);
            @SuppressWarnings("unchecked")
            final Copy<?, ?> copyEntry =
                Copy.Safely.to(Keyed.value().at(key).of(copy.getTargetPosition()),
                    Property.at(propertyName).of(copy.getSourcePosition()));
            if (context.evalSuccess(copyEntry)) {
                result = true;
            }
        }
        return result;
    }

    /**
     * If at least one property name can be converted to an assignable key, say the operation is supported and we'll
     * give it a shot.
     */
    @Override
    public boolean supports(TherianContext context, Copy<? extends Object, ? extends Map> copy) {
        if (!super.supports(context, copy)) {
            return false;
        }

        final Type targetKeyType = getKeyType(copy.getTargetPosition());

        final Position.ReadWrite<?> targetKey = Positions.readWrite(targetKeyType);
        for (String propertyName : getProperties(context, copy.getSourcePosition())) {
            if (context.supports(Convert.to(targetKey, Positions.readOnly(propertyName)))) {
                return true;
            }
        }
        return false;
    }

    private Iterable<String> getProperties(TherianContext context, Position.Readable<?> source) {
        return FilteredIterable.of(BeanProperties.getPropertyNames(context, source)).retain(notIgnored);
    }

    private Type getKeyType(Position<? extends Map> target) {
        return ObjectUtils.defaultIfNull(
            Types.unrollVariables(TypeUtils.getTypeArguments(target.getType(), Map.class),
                Map.class.getTypeParameters()[0]), Object.class);
    }
}
