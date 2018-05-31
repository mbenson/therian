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
package therian.util;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.reflect.TypeUtils;

/**
 * Decorates a {@link Map} of {@link TypeVariable} to {@link Type} rendering {@link TypeVariable} keys and values for
 * easier inspection.
 */
public class TypeVariableMap extends AbstractMap<TypeVariable<?>, Type> {

    public static TypeVariableMap wrap(Map<TypeVariable<?>, Type> m) {
        if (m instanceof TypeVariableMap) {
            return (TypeVariableMap) m;
        }
        return new TypeVariableMap(m == null ? Collections.emptyMap() : m);
    }

    private static String toString(Type type) {
        if (type instanceof TypeVariable<?>) {
            return TypeUtils.toLongString((TypeVariable<?>) type);
        }
        return Types.toString(type);
    }

    private final Map<TypeVariable<?>, Type> wrapped;

    public TypeVariableMap() {
        this(new HashMap<>());
    }

    private TypeVariableMap(Map<TypeVariable<?>, Type> wrapped) {
        super();
        this.wrapped = wrapped;
    }

    @Override
    public Set<Map.Entry<TypeVariable<?>, Type>> entrySet() {
        final Set<Map.Entry<TypeVariable<?>, Type>> wrappedEntries = wrapped.entrySet();
        return new AbstractSet<Map.Entry<TypeVariable<?>, Type>>() {

            @Override
            public Iterator<java.util.Map.Entry<TypeVariable<?>, Type>> iterator() {
                final Iterator<java.util.Map.Entry<TypeVariable<?>, Type>> wrappedIterator = wrappedEntries.iterator();
                return new Iterator<Map.Entry<TypeVariable<?>, Type>>() {

                    @Override
                    public void remove() {
                        wrappedIterator.remove();
                    }

                    @Override
                    public Map.Entry<TypeVariable<?>, Type> next() {
                        final Map.Entry<TypeVariable<?>, Type> wrappedEntry = wrappedIterator.next();
                        return new Map.Entry<TypeVariable<?>, Type>() {

                            @Override
                            public TypeVariable<?> getKey() {
                                return wrappedEntry.getKey();
                            }

                            @Override
                            public Type getValue() {
                                return wrappedEntry.getValue();
                            }

                            @Override
                            public Type setValue(Type value) {
                                return wrappedEntry.setValue(value);
                            }

                            @Override
                            public String toString() {
                                return TypeVariableMap.toString(getKey()) + '=' + TypeVariableMap.toString(getValue());
                            }
                        };
                    }

                    @Override
                    public boolean hasNext() {
                        return wrappedIterator.hasNext();
                    }
                };
            }

            @Override
            public int size() {
                return wrapped.size();
            }
        };
    }

    @Override
    public Type put(TypeVariable<?> key, Type value) {
        return wrapped.put(key, value);
    }

    @Override
    public Type remove(Object key) {
        return wrapped.remove(key);
    }

    @Override
    public String toString() {
        return entrySet().toString().replaceFirst("^\\[", "{").replaceFirst("\\]$", "}");
    }
}
