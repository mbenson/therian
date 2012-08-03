package therian.position.relative;

import java.beans.FeatureDescriptor;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.functor.UnaryPredicate;
import org.apache.commons.functor.core.collection.FilteredIterable;
import org.apache.commons.functor.generator.IteratorToGeneratorAdapter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.reflect.TypeUtils;

import therian.TherianContext;
import therian.el.ELConstants;
import therian.position.Position;
import therian.position.Position.Readable;
import therian.position.relative.RelativePosition.ReadWrite;
import therian.util.Types;

/**
 * Provides fluent access to {@link RelativePositionFactory} instances for {@link Map} keyed values. e.g.
 * Keyed.<MetasyntacticVariable> value().at("foo").of(Collections.singletonMap("foo", MetasyntacticVariable.FOO);
 */
public class Keyed {

    public static class PositionFactory<K, V> extends RelativePositionFactory<Map<K, V>, V> {
        private final K key;

        @SuppressWarnings("unchecked")
        protected PositionFactory(K key) {
            super(new GetTypeMixin<V>(key), new RelativePosition.Mixin.ELValue<V>(key));
            this.key = key;
        }

        @Override
        public <P extends Map<K, V>> RelativePosition.ReadWrite<P, V> of(Readable<P> parentPosition) {
            return (ReadWrite<P, V>) super.of(parentPosition);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof PositionFactory == false) {
                return false;
            }
            return ObjectUtils.equals(key, ((PositionFactory<?, ?>) obj).key);
        }
    }

    public static class Value<V> {
        public <K> PositionFactory<K, V> at(K key) {
            return new PositionFactory<K, V>(key);
        }
    }

    private static class GetTypeMixin<V> implements RelativePosition.GetType<V> {
        private final Object key;

        private GetTypeMixin(Object key) {
            super();
            this.key = key;
        }

        public <P> Type getType(Readable<? extends P> parentPosition) {
            return Types.refine(getBasicType(parentPosition), parentPosition.getType());
        }

        private <P> Type getBasicType(final Position.Readable<? extends P> parentPosition) {
            final TherianContext context = TherianContext.getInstance();
            final P parent = parentPosition.getValue();
            final UnaryPredicate<FeatureDescriptor> filter = new UnaryPredicate<FeatureDescriptor>() {
                public boolean test(FeatureDescriptor obj) {
                    return String.valueOf(key).equals(obj.getName());
                }
            };

            final Iterable<FeatureDescriptor> featureDescriptors =
                parent == null ? Collections.<FeatureDescriptor> emptyList() : FilteredIterable.of(
                    IteratorToGeneratorAdapter.adapt(context.getELResolver().getFeatureDescriptors(context, parent))
                        .toCollection()).retain(filter);

            for (FeatureDescriptor feature : featureDescriptors) {
                final Type fromGenericTypeAttribute = Type.class.cast(feature.getValue(ELConstants.GENERIC_TYPE));
                if (fromGenericTypeAttribute != null) {
                    return fromGenericTypeAttribute;
                }
            }

            return ObjectUtils.defaultIfNull(
                TypeUtils.getTypeArguments(parentPosition.getType(), Map.class).get(Map.class.getTypeParameters()[1]),
                Object.class);
        }
    }

    private Keyed() {
    }

    public static <V> Value<V> value() {
        return new Value<V>();
    }
}
