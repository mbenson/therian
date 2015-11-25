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
package therian.cdi.internal.literal;

import javax.enterprise.util.AnnotationLiteral;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class EmptyAnnotationLiteral<T extends Annotation> extends AnnotationLiteral<T> {
    private Class<T> annotationType;

    protected EmptyAnnotationLiteral() {
        // no-op
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        if (annotationType == null) {
            annotationType = getAnnotationType(getClass());
        }
        return annotationType;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(final Object other) {
        return Annotation.class.isInstance(other) &&
            Annotation.class.cast(other).annotationType().equals(annotationType());
    }

    private Class<T> getAnnotationType(final Class<?> definedClazz) {
        final Type superClazz = definedClazz.getGenericSuperclass();
        if (superClazz.equals(Object.class)) {
            throw new IllegalArgumentException("Super class must be parametrized type!");
        } else if (superClazz instanceof ParameterizedType) {
            final ParameterizedType paramType = (ParameterizedType) superClazz;
            final Type[] actualArgs = paramType.getActualTypeArguments();

            if (actualArgs.length == 1) {
                final Type type = actualArgs[0];
                if (type instanceof Class) {
                    return (Class<T>) type;
                } else {
                    throw new IllegalArgumentException("Not class type!");
                }
            } else {
                throw new IllegalArgumentException("More than one parametric type!");
            }
        }
        return getAnnotationType((Class<?>) superClazz);
    }
}
