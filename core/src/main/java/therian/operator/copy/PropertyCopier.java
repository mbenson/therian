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
package therian.operator.copy;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.Typed;
import org.apache.commons.lang3.tuple.Pair;

import therian.OperationException;
import therian.Operator.DependsOn;
import therian.BindTypeVariable;
import therian.Hint;
import therian.OperatorDefinitionException;
import therian.TherianContext;
import therian.operation.Copy;
import therian.operator.convert.DefaultCopyingConverter;
import therian.operator.convert.ELCoercionConverter;
import therian.operator.convert.NOPConverter;
import therian.position.Position;
import therian.position.relative.Expression;
import therian.position.relative.Property;
import therian.position.relative.RelativePositionFactory;
import therian.util.BeanProperties;
import therian.util.BeanProperties.ReturnProperties;
import uelbox.UEL;

/**
 * Copies based on annotations. Concrete subclasses must specify one or both of {@link Mapping} and {@link Matching} to
 * designate property/expression (using
 *
 * <pre>
 * #{}
 * </pre>
 *
 * embedding syntax). Alternatively, consider using {@link PropertyCopier#getInstance(Typed, Typed, Mapping, Matching)}.
 *
 * @param <SOURCE>
 * @param <TARGET>
 */
@DependsOn({ ConvertingCopier.class, NOPConverter.class, ELCoercionConverter.class, DefaultCopyingConverter.class })
public abstract class PropertyCopier<SOURCE, TARGET> extends Copier<SOURCE, TARGET> {

    /**
     * Configures a {@link PropertyCopier} for property mapping, using a fluent syntax of
     * "mapping: value from foo to bar, value from x to y, etc.".
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD })
    public @interface Mapping {

        /**
         * Specifies mapping for a property value.
         */
        @Repeatable(Mapping.class)
        public @interface Value {

            /**
             * Property name or delimited expression; blank implies source {@link Position}.
             */
            String from() default "";

            /**
             * Property name or delimited expression; blank implies target {@link Position}.
             */
            String to() default "";
        }

        /**
         * Mapping {@link Value}s
         */
        Value[] value();
    }

    /**
     * Configures a {@link PropertyCopier} for property matching.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD })
    public @interface Matching {

        /**
         * Property names to match. An empty array implies all matching properties that are readable on the source and
         * writable on the target.
         */
        String[] value() default {};

        /**
         * Property names to exclude from matching.
         */
        String[] exclude() default {};
    }

    /**
     * Describes the {@link PropertyCopier}'s behavior when presented with a {@code null} source value.
     *
     * @since 0.2
     */
    public enum NullBehavior implements Hint {
        /**
         * Indicates that the operation will be unsupported.
         */
        UNSUPPORTED,

        /**
         * Indicates that the operation will be supported, but will do nothing.
         */
        NOOP,

        /**
         * Indicates that the operation will be implemented by copying {@code null} values to target properties.
         */
        COPY_NULLS;

        @Override
        public Class<? extends Hint> getType() {
            return NullBehavior.class;
        }
    }

    /**
     * Result from
     * {@link PropertyCopier#getInstance(Typed, Typed, therian.operator.copy.PropertyCopier.Mapping, therian.operator.copy.PropertyCopier.Matching)}.
     *
     * @param <SOURCE>
     * @param <TARGET>
     */
    public static class FromFactory<SOURCE, TARGET> extends PropertyCopier<SOURCE, TARGET> {
        private final Typed<SOURCE> sourceType;
        private final Typed<TARGET> targetType;

        private FromFactory(Typed<SOURCE> sourceType, Typed<TARGET> targetType, Mapping mapping, Matching matching) {
            super(mapping, matching);
            this.sourceType = sourceType;
            this.targetType = targetType;
        }

        @BindTypeVariable
        public Typed<SOURCE> getSourceType() {
            return sourceType;
        }

        @BindTypeVariable
        public Typed<TARGET> getTargetType() {
            return targetType;
        }
    }

    /**
     * Create a {@link PropertyCopier} instance at runtime.
     * 
     * @param sourceType
     * @param targetType
     * @param mapping
     * @param matching
     * @return PropertyCopier
     */
    public static <SOURCE, TARGET> PropertyCopier<SOURCE, TARGET> getInstance(Typed<SOURCE> sourceType,
        Typed<TARGET> targetType, Mapping mapping, Matching matching) {
        return new FromFactory<SOURCE, TARGET>(Validate.notNull(sourceType, "sourceType"), Validate.notNull(targetType,
            "targetType"), mapping, matching);
    }

    private static List<Pair<RelativePositionFactory.ReadWrite<Object, ?>, RelativePositionFactory.ReadWrite<Object, ?>>> parse(
        Mapping mapping) {
        final List<Pair<RelativePositionFactory.ReadWrite<Object, ?>, RelativePositionFactory.ReadWrite<Object, ?>>> m =
            new ArrayList<>();

        Validate.validState(mapping.value().length > 0, "@Mapping cannot be empty");

        final boolean optional = true;

        for (Mapping.Value v : mapping.value()) {
            final String from = StringUtils.trimToNull(v.from());
            final String to = StringUtils.trimToNull(v.to());

            Validate.validState(from != null || to != null,
                "both from and to cannot be blank/empty for a single @Mapping.Value");

            final RelativePositionFactory.ReadWrite<Object, ?> target = toFactory(to, !optional);
            final RelativePositionFactory.ReadWrite<Object, ?> source = toFactory(from, optional);

            m.add(Pair.<RelativePositionFactory.ReadWrite<Object, ?>, RelativePositionFactory.ReadWrite<Object, ?>> of(
                source, target));
        }
        return Collections.unmodifiableList(m);
    }

    private static RelativePositionFactory.ReadWrite<Object, ?> toFactory(String s, boolean optional) {
        if (StringUtils.isBlank(s)) {
            return null;
        }
        if (UEL.isDelimited(s)) {
            return optional ? Expression.optional(s) : Expression.at(s);
        }
        return optional ? Property.optional(s) : Property.at(s);
    }

    private final List<Pair<RelativePositionFactory.ReadWrite<Object, ?>, RelativePositionFactory.ReadWrite<Object, ?>>> mappings;
    private final Matching matching;

    protected PropertyCopier() {
        try {
            @SuppressWarnings("rawtypes")
            final Class<? extends PropertyCopier> c = getClass();
            matching = c.getAnnotation(Matching.class);

            final Mapping mapping = c.getAnnotation(Mapping.class);
            if (mapping == null) {
                Validate.validState(c.isAnnotationPresent(Matching.class),
                    "%s specifies neither @Mapping nor @Matching", c);
                mappings = Collections.emptyList();
            } else {
                mappings = parse(mapping);
            }
        } catch (Exception e) {
            throw new OperatorDefinitionException(this, e);
        }
    }

    private PropertyCopier(Mapping mapping, Matching matching) {
        try {
            @SuppressWarnings("rawtypes")
            final Class<? extends PropertyCopier> c = getClass();
            this.matching = matching;

            if (mapping == null) {
                Validate.validState(c.isAnnotationPresent(Matching.class),
                    "%s specifies neither @Mapping nor @Matching", c);
                mappings = Collections.emptyList();
            } else {
                mappings = parse(mapping);
            }
        } catch (Exception e) {
            throw new OperatorDefinitionException(this, e);
        }
    }

    @Override
    public boolean perform(TherianContext context, Copy<? extends SOURCE, ? extends TARGET> copy) {
        final NullBehavior nullBehavior;
        if (copy.getSourcePosition().getValue() == null) {
            nullBehavior = context.getTypedContext(NullBehavior.class, defaultNullBehavior());
        } else {
            nullBehavior = null;
        }

        if (nullBehavior == NullBehavior.UNSUPPORTED) {
            return false;
        }
        final Iterable<Copy<?, ?>> mapped = map(context, copy);
        if (nullBehavior == NullBehavior.NOOP && mapped.iterator().hasNext()) {
            return true;
        }
        final Iterable<Copy<?, ?>> matched = match(context, copy);
        if (nullBehavior == NullBehavior.NOOP && matched.iterator().hasNext()) {
            return true;
        }

        boolean result = handle(context, Phase.EVALUATION, mapped);
        result = handle(context, Phase.EVALUATION, matched) || result;
        return result;
    }

    @Override
    public boolean supports(TherianContext context, Copy<? extends SOURCE, ? extends TARGET> copy) {
        if (!super.supports(context, copy)) {
            return false;
        }
        if (copy.getSourcePosition().getValue() == null) {
            if (context.getTypedContext(NullBehavior.class, defaultNullBehavior()) == NullBehavior.UNSUPPORTED) {
                return false;
            }
        }

        return handle(context, Phase.SUPPORT_CHECK, map(context, copy))
            || handle(context, Phase.SUPPORT_CHECK, match(context, copy));
    }

    private boolean handle(TherianContext context, Phase phase, Iterable<Copy<?, ?>> nestedOperations) {
        boolean result = false;
        for (Copy<?, ?> nestedCopy : nestedOperations) {
            switch (phase) {
            case SUPPORT_CHECK:
                // safe copies have already been verified supported:
                if (!(nestedCopy instanceof Copy.Safely || context.supports(nestedCopy))) {
                    return false;
                }
                result = true;
                break;
            case EVALUATION:
                if (!context.evalSuccess(nestedCopy)) {
                    throw new OperationException(nestedCopy);
                }
                result = true;
                break;
            default:
                throw new IllegalArgumentException("phase " + phase);
            }
        }
        return result;
    }

    protected NullBehavior defaultNullBehavior() {
        return NullBehavior.NOOP;
    }

    private Position.Readable<?> dereference(RelativePositionFactory.ReadWrite<Object, ?> positionFactory,
        Position.Readable<?> parentPosition) {
        return positionFactory == null ? parentPosition : positionFactory.of(parentPosition);
    }

    private Iterable<Copy<?, ?>> map(final TherianContext context, Copy<? extends SOURCE, ? extends TARGET> copy) {
        if (mappings.isEmpty()) {
            return Collections.emptySet();
        }
        final List<Copy<?, ?>> result = new ArrayList<>();

        for (Pair<RelativePositionFactory.ReadWrite<Object, ?>, RelativePositionFactory.ReadWrite<Object, ?>> mapping : mappings) {
            final Position.Readable<?> propertySource = dereference(mapping.getLeft(), copy.getSourcePosition());
            final Position.Readable<?> propertyTarget = dereference(mapping.getRight(), copy.getTargetPosition());
            result.add(Copy.to(propertyTarget, propertySource));
        }
        return result;
    }

    private Iterable<Copy<?, ?>> match(final TherianContext context, Copy<? extends SOURCE, ? extends TARGET> copy) {
        if (matching == null) {
            return Collections.emptySet();
        }
        final Set<String> properties = new HashSet<>(Arrays.asList(matching.value()));

        // if no properties were explicitly specified for matching, take whatever we can get
        final boolean lenient = properties.isEmpty();
        if (lenient) {
            properties.addAll(BeanProperties.getPropertyNames(ReturnProperties.WRITABLE, context,
                copy.getTargetPosition()));
            properties.retainAll(BeanProperties.getPropertyNames(ReturnProperties.ALL, context,
                copy.getSourcePosition()));
        }
        properties.removeAll(Arrays.asList(matching.exclude()));

        final List<Copy<?, ?>> result = new ArrayList<>();
        for (String property : properties) {
            if (StringUtils.isBlank(property)) {
                continue;
            }
            final RelativePositionFactory.ReadWrite<Object, ?> factory = Property.optional(property);
            final Position.Readable<?> target = dereference(factory, copy.getTargetPosition());
            final Position.Readable<?> source = dereference(factory, copy.getSourcePosition());

            if (lenient) {
                final Copy<?, ?> propertyCopy = Copy.Safely.to(target, source);
                if (context.supports(propertyCopy)) {
                    result.add(propertyCopy);
                }
            } else {
                result.add(Copy.to(target, source));
            }
        }
        return result;
    }

}
