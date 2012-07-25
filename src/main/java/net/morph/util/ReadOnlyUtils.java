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
package net.morph.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Deque;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * Read-only utils.
 */
public class ReadOnlyUtils {
    public static <T> Deque<T> wrap(Deque<T> deque) {
        @SuppressWarnings("unchecked")
        final Deque<T> result =
            (Deque<T>) Proxy.newProxyInstance(Validate.notNull(deque, "object required to wrap").getClass()
                .getClassLoader(), new Class[] { Deque.class }, new InvocationHandler() {
                public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                    if (StringUtils.startsWithAny(method.getName(), "add", "offer", "poll", "pop", "push", "remove",
                        "clear", "retain")) {
                        throw new UnsupportedOperationException("read-only");
                    }
                    final Object result = method.invoke(o, objects);
                    if (StringUtils.endsWithIgnoreCase(method.getName(), "iterator")) {
                        final Iterator<?> wrapIterator = (Iterator<?>) result;
                        return new Iterator<Object>() {
                            public boolean hasNext() {
                                return wrapIterator.hasNext();
                            }

                            public Object next() {
                                return wrapIterator.next();
                            }

                            public void remove() {
                            }
                        };
                    }
                    return null;
                }
            });
        return result;
    }

    public static <T> Iterator<T> wrap(final Iterator<T> iterator) {
        return new Iterator<T>() {
            public boolean hasNext() {
                return iterator.hasNext();
            }

            public T next() {
                return iterator.next();
            }

            public void remove() {
                throw new UnsupportedOperationException("read-only");
            }
        };
    }
}
