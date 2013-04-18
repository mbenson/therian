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

import java.lang.reflect.Type;
import java.util.Collection;

import org.apache.commons.lang3.reflect.TypeUtils;

import therian.Operator;
import therian.TherianContext;
import therian.operation.Convert;
import therian.operation.GetElementType;
import therian.position.Box;
import therian.util.Types;

/**
 * Converts to array using the following approach:
 * <ul>
 * <li>convert source to {@link Iterable}</li>
 * <li>thence to collection of target element type</li>
 * <li>thence to array</li>
 * </ul>
 */
public class DefaultToArrayConverter implements Operator<Convert<?, ?>> {

    public boolean perform(TherianContext context, final Convert<?, ?> convert) {

        // if element type not available, assume we're wrapping an arbitrary object as a singleton
        final Type sourceElementType =
            context.evalIfSupported(GetElementType.of(convert.getSourcePosition()), convert.getSourcePosition()
                .getType());

        // as advertised, we first convert our source position to an iterable of source element type
        @SuppressWarnings({ "rawtypes", "unchecked" })
        final Box<Iterable<?>> sourceIterable = new Box(Types.parameterize(Iterable.class, sourceElementType));

        final Convert<?, Iterable<?>> sourceToIterable = Convert.to(sourceIterable, convert.getSourcePosition());
        if (!context.evalSuccessIfSupported(sourceToIterable)) {
            return false;
        }
        final Type targetElementType = TypeUtils.getArrayComponentType(convert.getTargetPosition().getType());

        // next, we convert our source iterable to a collection of target element type
        @SuppressWarnings({ "unchecked", "rawtypes" })
        final Box<Collection<?>> targetElementCollection =
            new Box(Types.parameterize(Collection.class, targetElementType));

        final Convert<Iterable<?>, Collection<?>> sourceIterableToTargetElementCollection =
            Convert.to(targetElementCollection, sourceIterable);
        if (!context.evalSuccessIfSupported(sourceIterableToTargetElementCollection)) {
            return false;
        }
        // finally, convert that collection to an array now that its size has stabilized
        final Convert<Collection<?>, ?> targetElementCollectionToArray =
            Convert.to(convert.getTargetPosition(), targetElementCollection);
        return context.evalSuccessIfSupported(targetElementCollectionToArray);
    }

    public boolean supports(TherianContext context, Convert<?, ?> convert) {
        // too much work to figure the whole thing; just try it when the time comes
        return TypeUtils.isArrayType(convert.getTargetPosition().getType())
            && context.supports(Convert.to(Iterable.class, convert.getSourcePosition()));
    }

}
