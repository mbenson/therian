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

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.TypeUtils;

import therian.TherianContext;
import therian.buildweaver.StandardOperator;
import therian.operation.Convert;
import therian.position.Position.Readable;

/**
 * DefaultCopyingConverter; tries:
 * <ul>
 * <li>constructor(source)</li>
 * <li>default constructor</li>
 * </ul>
 * for non-abstract target types.
 */
@StandardOperator
public class DefaultCopyingConverter extends Converter.WithDynamicTarget<Object> {

    @SuppressWarnings("rawtypes")
    private static class Delegate extends CopyingConverter {
        private static Constructor<?> getConstructor(Convert<?, ?> convert) {
            final Class<?> rawTargetType = TypeUtils.getRawType(convert.getTargetPosition().getType(), null);
            if ((rawTargetType.getModifiers() & Modifier.ABSTRACT) > 0) {
                return null;
            }
            final Constructor<?> result =
                ConstructorUtils.getMatchingAccessibleConstructor(rawTargetType,
                    ClassUtils.toClass(convert.getSourcePosition().getValue()));
            return result == null ? convert.getSourcePosition().getValue() == null ? null : ConstructorUtils
                .getAccessibleConstructor(rawTargetType) : result;
        }

        private final Convert<?, ?> convert;

        /**
         * @param convert
         */
        private Delegate(Convert<?, ?> convert) {
            this.convert = convert;
        }

        @Override
        protected Object createCopyDestination(Readable readable) throws Exception {
            final Constructor<?> constructor = getConstructor(convert);
            return constructor.getParameterTypes().length == 0 ? constructor.newInstance() : constructor
                .newInstance(readable.getValue());
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean supports(TherianContext context, Convert convert) {
            return super.supports(context, convert) && getConstructor(convert) != null;
        }

    }

    // specifically avoid doing typed ops as we want to catch stuff that slips through the cracks
    @Override
    @SuppressWarnings({ "unchecked" })
    public boolean perform(TherianContext context, final Convert<?, ?> convert) {
        return new Delegate(convert).perform(context, convert);
    }

    @Override
    public boolean supports(TherianContext context, Convert<?, ?> convert) {
        return new Delegate(convert).supports(context, convert);
    }

}
