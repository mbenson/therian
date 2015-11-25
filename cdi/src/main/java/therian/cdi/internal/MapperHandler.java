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

import therian.Operator;
import therian.Operators;
import therian.Therian;
import therian.TherianContext;
import therian.TherianModule;
import therian.operation.Copy;
import therian.operator.copy.PropertyCopier;
import therian.util.Positions;

import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class MapperHandler implements InvocationHandler {
    private final Map<Method, Meta<?, ?>> mapping;
    private final String toString;

    public MapperHandler(final AnnotatedType<?> type) {
        // just for error handling
        of(type.getMethods().stream()
            .filter(m -> m.isAnnotationPresent(PropertyCopier.Mapping.class) && (m.getParameters().size() != 1 || m.getJavaMember().getReturnType() == void.class))
            .collect(toList()))
            .filter(l -> !l.isEmpty())
            .ifPresent(l -> {
                throw new IllegalArgumentException("@Mapping only supports one parameter and not void signatures");
            });

        this.mapping = type.getMethods().stream()
            .filter(m -> m.isAnnotationPresent(PropertyCopier.Mapping.class))
            .collect(toMap(
                AnnotatedMethod::getJavaMember,
                am -> {
                    final Method member = am.getJavaMember();
                    final Type from = member.getGenericParameterTypes()[0];
                    final Class to = member.getReturnType();

                    final Collection<Operator<?>> operators = new ArrayList<>();
                    operators.add(new PropertyCopier(
                        am.getAnnotation(PropertyCopier.Mapping.class),
                        am.getAnnotation(PropertyCopier.Matching.class),
                        from, to) {
                    });
                    operators.addAll(asList(Operators.standard()));

                    final Therian therian = Therian.usingModules(TherianModule.create()
                        .withOperators(operators.toArray(new Operator[operators.size()])));

                    return new Meta(
                        therian,
                        () -> {
                            try {
                                return to.newInstance();
                            } catch (final IllegalAccessException | InstantiationException e) {
                                throw new IllegalStateException(e);
                            }
                        },
                        (sourceInstance, targetInstance) -> Copy.to(
                            Positions.readWrite(to, targetInstance),
                            Positions.readOnly(from, sourceInstance)));
                }));

        this.toString = getClass().getSimpleName() + "[" + type.getJavaClass().getName() + "]";
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            try {
                return method.invoke(this, args);
            } catch (final InvocationTargetException ite) {
                throw ite.getCause();
            }
        }

        final Meta meta = mapping.get(method);
        return meta.copy(args[0]);
    }

    @Override
    public String toString() {
        return toString;
    }

    private static final class Meta<A, B> {
        private final Supplier<B> newInstance;
        private final BiFunction<A, B, Copy<A, B>> copy;
        private final Therian therian;

        public Meta(final Therian therian, final Supplier<B> newInstance, final BiFunction<A, B, Copy<A, B>> copy) {
            this.therian = therian;
            this.newInstance = newInstance;
            this.copy = copy;
        }

        public B copy(final A in) {
            final TherianContext context = therian.context();
            final B out = newInstance.get();
            final Copy<A, B> operation = copy.apply(in, out);

            final boolean success = context.evalSuccess(operation);
            if (success) {
                return out;
            }

            throw new IllegalStateException("can't map " + in);
        }
    }
}
