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
package therian.cdi.internal;

import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Function;

import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;

import org.apache.commons.lang3.reflect.TypeUtils;

import therian.Therian;
import therian.TherianModule;
import therian.operation.Convert;
import therian.operator.copy.PropertyCopier;
import therian.util.Positions;

public class MapperHandler implements InvocationHandler {
    private final Map<Method, Meta<?, ?>> mapping;
    private final String toString;

    public MapperHandler(final AnnotatedType<?> type) {
        // just for error handling
        of(type.getMethods().stream()
            .filter(m -> m.isAnnotationPresent(PropertyCopier.Mapping.class)
                && (m.getParameters().size() != 1 || m.getJavaMember().getReturnType() == void.class))
            .collect(toList())).filter(l -> !l.isEmpty()).ifPresent(l -> {
                throw new IllegalArgumentException("@Mapping only supports one parameter and not void signatures");
            });

        // TODO: use a single Therian instance if there are not redundant conversions specified by interface methods

        this.mapping = type.getMethods().stream().filter(m -> m.isAnnotationPresent(PropertyCopier.Mapping.class))
            .collect(toMap(AnnotatedMethod::getJavaMember, am -> {
                final Method member = am.getJavaMember();
                final Type from = member.getGenericParameterTypes()[0];
                final Type to = member.getGenericReturnType();

                final Therian therian = Therian.standard()
                    .withAdditionalModules(TherianModule.create()
                        .withOperators(PropertyCopier.getInstance(TypeUtils.wrap(from), TypeUtils.wrap(to),
                            am.getAnnotation(PropertyCopier.Mapping.class),
                            am.getAnnotation(PropertyCopier.Matching.class))));

                @SuppressWarnings({ "unchecked", "rawtypes" })
                final Meta<?, ?> result = new Meta(therian, sourceInstance -> Convert.to(TypeUtils.wrap(to),
                    Positions.<Object> readOnly(from, sourceInstance)));
                return result;
            }));

        this.toString = getClass().getSimpleName() + "[" + type.getJavaClass().getName() + "]";
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (Object.class.equals(method.getDeclaringClass())) {
            try {
                return method.invoke(this, args);
            } catch (final InvocationTargetException ite) {
                throw ite.getCause();
            }
        }

        @SuppressWarnings("unchecked")
        final Meta<Object, Object> meta = (Meta<Object, Object>) mapping.get(method);
        return meta.convert(args[0]);
    }

    @Override
    public String toString() {
        return toString;
    }

    private static final class Meta<A, B> {
        private final Therian therian;
        private final Function<A, Convert<A, B>> convert;

        public Meta(final Therian therian, final Function<A, Convert<A, B>> convert) {
            this.therian = therian;
            this.convert = convert;
        }

        public B convert(final A in) {
            return therian.context().eval(convert.apply(in));
        }
    }
}
