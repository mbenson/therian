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
package therian.operator.immutablecheck;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.ClassUtils;

/**
 * Checks for types universally known to be immutable.
 */
public class DefaultImmutableChecker extends ImmutableChecker {
    private static final Set<Class<?>> KNOWN_IMMUTABLE_TYPES;

    static {
        final HashSet<Class<?>> s = new HashSet<Class<?>>();
        s.addAll(Arrays.<Class<?>> asList(String.class, Enum.class, Annotation.class));
        s.add(Collections.emptySet().getClass());
        s.add(Collections.unmodifiableSet(Collections.emptySet()).getClass());
        s.add(Collections.emptyList().getClass());
        s.add(Collections.unmodifiableList(Collections.emptyList()).getClass());
        s.add(Collections.unmodifiableSortedSet(new TreeSet<Object>()).getClass());
        s.add(Collections.emptyMap().getClass());
        s.add(Collections.unmodifiableMap(Collections.emptyMap()).getClass());
        s.add(Collections.unmodifiableSortedMap(new TreeMap<String, Object>()).getClass());
        KNOWN_IMMUTABLE_TYPES = Collections.unmodifiableSet(s);
    }

    @Override
    protected boolean isImmutable(Object object) {
        if (object == null) {
            return true;
        }
        final Class<?> cls = object.getClass();
        if (cls.isPrimitive()) {
            return true;
        }
        if (ClassUtils.wrapperToPrimitive(cls) != null) {
            return true;
        }
        for (final Class<?> type : KNOWN_IMMUTABLE_TYPES) {
            if (type.isInstance(object)) {
                return true;
            }
        }
        return cls.equals(Object.class);
    }
}
