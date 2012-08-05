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
package therian.operator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.TypeUtils;

import therian.Operator;
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
public class DefaultCopyingConverter implements Operator<Convert<?, ?>> {
    private static final DefaultCopyingConverter INSTANCE = new DefaultCopyingConverter();

    public DefaultCopyingConverter() {
    }

    // specifically avoid doing typed ops as we want to catch stuff that slips through the cracks
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void perform(final Convert<?, ?> convert) {
        new CopyingConverter() {

            @Override
            protected Object createCopyDestination(Readable readable) throws Exception {
                final Constructor<?> constructor = getConstructor(convert);
                return constructor.getParameterTypes().length == 0 ? constructor.newInstance() : constructor
                    .newInstance(readable.getValue());
            }

        }.perform(convert);
    }

    public boolean supports(Convert<?, ?> convert) {
        return getConstructor(convert) != null;
    }

    private Constructor<?> getConstructor(Convert<?, ?> convert) {
        final Class<?> rawTargetType = TypeUtils.getRawType(convert.getTargetPosition().getType(), null);
        if ((rawTargetType.getModifiers() & Modifier.ABSTRACT) > 0) {
            return null;
        }
        final Constructor<?> result =
            ConstructorUtils.getMatchingAccessibleConstructor(rawTargetType,
                ClassUtils.toClass(convert.getSourcePosition().getValue()));
        return result == null ? ConstructorUtils.getAccessibleConstructor(rawTargetType) : result;
    }

    public static DefaultCopyingConverter instance() {
        return INSTANCE;
    }
}
