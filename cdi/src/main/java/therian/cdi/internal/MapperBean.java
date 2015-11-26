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

import therian.cdi.internal.literal.AnyLiteral;
import therian.cdi.internal.literal.DefaultLiteral;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;

public class MapperBean<T> implements Bean<T> {
    private final Set<Type> types;
    private final Set<Annotation> qualifiers;
    private final Class<T> clazz;
    private final Class<?>[] proxyTypes;
    private final MapperHandler handler;

    public MapperBean(final AnnotatedType at) {
        clazz = at.getJavaClass();
        types = new HashSet<>(asList(clazz, Object.class));
        qualifiers = new HashSet<>(asList(DefaultLiteral.INSTANCE, AnyLiteral.INSTANCE));
        proxyTypes = new Class<?>[] { clazz };
        handler = new MapperHandler(at);
    }

    @Override
    public Set<Type> getTypes() {
        return types;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return qualifiers;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return ApplicationScoped.class;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return emptySet();
    }

    @Override
    public Class<?> getBeanClass() {
        return clazz;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return emptySet();
    }

    @Override
    public boolean isAlternative() {
        return false;
    }

    @Override
    public T create(final CreationalContext<T> context) {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        return (T) Proxy.newProxyInstance(
            contextClassLoader == null ? ClassLoader.getSystemClassLoader() : contextClassLoader,
            proxyTypes, handler);
    }

    @Override
    public void destroy(final T instance, final CreationalContext<T> context) {
        // no-op
    }
}
