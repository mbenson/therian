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
package therian.position.relative;

import java.beans.FeatureDescriptor;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.reflect.TypeUtils;

import therian.TherianContext;
import therian.el.ELConstants;
import therian.position.Position.Readable;
import therian.util.Types;

/**
 * Provides fluent access to {@link RelativePositionFactory} instances for {@link Map} keyed values. e.g.
 * Keyed.<MetasyntacticVariable> value().at("foo").of(Collections.singletonMap("foo", MetasyntacticVariable.FOO);
 */
public class Keyed {

    /**
     * Keyed value {@link RelativePositionFactory}.
     *
     * @param <K>
     * @param <V>
     */
    public static class PositionFactory<K, V> extends RelativePositionFactory.ReadWrite<Map<K, V>, V> {

        private final K key;

        private PositionFactory(K key) {
            this.key = key;
        }

        @Override
        public <P extends Map<K, V>> RelativePosition.ReadWrite<P, V> of(Readable<P> parentPosition) {
            class Result extends RelativePositionImpl<P, K> implements RelativePosition.ReadWrite<P, V> {

                Result(therian.position.Position.Readable<P> parentPosition, K name) {
                    super(parentPosition, name);
                }

                @Override
                public Type getType() {
                    return Types.refine(getBasicType(), parentPosition.getType());
                }

                private Type getBasicType() {
                    final TherianContext context = TherianContext.getInstance();
                    final P parent = parentPosition.getValue();

                    if (parent != null) {
                        try {
                            final String name = String.valueOf(key);
                            for (Iterator<FeatureDescriptor> fd =
                                IteratorUtils.filteredIterator(
                                    context.getELResolver().getFeatureDescriptors(context, parent),
                                    d -> name.equals(d.getName())); fd.hasNext();) {
                                final Type fromGenericTypeAttribute =
                                    Type.class.cast(fd.next().getValue(ELConstants.GENERIC_TYPE));
                                if (fromGenericTypeAttribute != null) {
                                    return fromGenericTypeAttribute;
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                    return ObjectUtils.defaultIfNull(TypeUtils.getTypeArguments(parentPosition.getType(), Map.class)
                        .get(Map.class.getTypeParameters()[1]), Object.class);
                }
            }
            return new Result(parentPosition, key);
        }

        @Override
        public String toString() {
            return String.format("Keyed Value [%s]", key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof PositionFactory == false) {
                return false;
            }
            return Objects.equals(key, ((PositionFactory<?, ?>) obj).key);
        }

        /**
         * Get the key.
         *
         * @return K
         */
        public K getKey() {
            return key;
        }
    }

    /**
     * Fluent step.
     *
     * @param <V>
     */
    public static class Value<V> {

        /**
         * "Keyed value at <em>key</em>".
         *
         * @param key
         * @return {@link PositionFactory}
         */
        public <K> PositionFactory<K, V> at(K key) {
            return new PositionFactory<>(key);
        }
    }

    private Keyed() {
    }

    /**
     * "Keyed value".
     *
     * @return {@link Value}
     */
    public static <V> Value<V> value() {
        return new Value<>();
    }
}
