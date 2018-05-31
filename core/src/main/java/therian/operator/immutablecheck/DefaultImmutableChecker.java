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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import therian.buildweaver.StandardOperator;

/**
 * Checks for types universally known to be immutable.
 */
@StandardOperator
public class DefaultImmutableChecker extends ImmutableChecker {

    private static final String[] KNOWN_IMMUTABLE_PREFIXES;
    private static final Set<Class<?>> KNOWN_IMMUTABLE_TYPES;

    static {
        KNOWN_IMMUTABLE_PREFIXES = new String[] { "immutable", "unmodifiable", "empty" };
        final Set<Class<?>> s = new HashSet<>();
        Collections.<Class<?>> addAll(s, String.class, Enum.class, Annotation.class, Class.class, Arrays.asList()
            .getClass());
        addTypeTo(s, Collections.emptySet());
        addTypeTo(s, Collections.unmodifiableSet(new HashSet<>()));
        addTypeTo(s, Collections.emptyList());
        addTypeTo(s, Collections.unmodifiableList(new ArrayList<>()));
        addTypeTo(s, Collections.unmodifiableSortedSet(new TreeSet<String>()));
        addTypeTo(s, Collections.emptyMap());
        addTypeTo(s, Collections.unmodifiableMap(new HashMap<>()));
        addTypeTo(s, Collections.unmodifiableSortedMap(new TreeMap<String, Object>()));

        Collections.addAll(s, Collections.singleton(null).getClass(), Collections.singletonList(null).getClass(),
            Collections.singletonMap(null, null).getClass());

        KNOWN_IMMUTABLE_TYPES = Collections.unmodifiableSet(s);
    }

    private static void addTypeTo(final Set<Class<?>> target, final Collection<?> coll) {
        addImmutableTypeTo(target, coll.getClass());
        addImmutableTypeTo(target, coll.iterator().getClass());
    }

    private static void addTypeTo(final Set<Class<?>> target, final List<?> list) {
        addTypeTo(target, (Collection<?>) list);
        addImmutableTypeTo(target, list.listIterator().getClass());
    }

    private static void addTypeTo(final Set<Class<?>> target, final SortedSet<String> sortedSet) {
        addTypeTo(target, (Collection<?>) sortedSet);
        addTypeTo(target, (Set<String>) sortedSet.headSet("foo"));
        addTypeTo(target, (Set<String>) sortedSet.tailSet("foo"));
        addTypeTo(target, (Set<String>) sortedSet.subSet("foo", "foo"));
    }

    private static void addTypeTo(final Set<Class<?>> target, final Map<?, ?> map) {
        addImmutableTypeTo(target, map.getClass());
        addTypeTo(target, map.keySet());
        addTypeTo(target, map.values());
    }

    private static void addTypeTo(final Set<Class<?>> target, final SortedMap<String, ?> sortedMap) {
        addTypeTo(target, (Map<String, ?>) sortedMap);
        addTypeTo(target, (Map<String, ?>) sortedMap.headMap("foo"));
        addTypeTo(target, (Map<String, ?>) sortedMap.tailMap("foo"));
        addTypeTo(target, (Map<String, ?>) sortedMap.subMap("foo", "foo"));
    }

    private static void addImmutableTypeTo(final Set<Class<?>> target, final Class<?> type) {
        if (target.contains(type)) {
            return;
        }
        Class<?> c = type;
        while (c.isAnonymousClass()) {
            c = c.getEnclosingClass();
        }
        if (target.contains(c) && !type.equals(c)
            || StringUtils.startsWithAny(c.getSimpleName().toLowerCase(Locale.US), KNOWN_IMMUTABLE_PREFIXES)) {
            target.add(type);
        }
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
        // quick check:
        if (KNOWN_IMMUTABLE_TYPES.contains(cls)) {
            return true;
        }
        // inheritance too:
        for (final Class<?> type : KNOWN_IMMUTABLE_TYPES) {
            if (type.isInstance(object)) {
                return true;
            }
        }
        return cls.equals(Object.class);
    }
}
