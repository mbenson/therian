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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import therian.OperationException;
import therian.TherianContext;
import therian.operation.Copy;
import therian.position.Position;
import therian.position.relative.Property;

/**
 * Copies based on annotated property mapping.
 * 
 * @param <SOURCE>
 * @param <TARGET>
 */
public abstract class PropertyCopier<SOURCE, TARGET> extends Copier<SOURCE, TARGET> {
    /**
     * Required on {@link PropertyCopier} subclasses.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Mapping {
        /**
         * Blank value implies the position itself.
         */
        public @interface Value {
            String from() default "";

            String to() default "";
        }

        Value[] value();
    }

    private final Mapping mapping;
    {
        @SuppressWarnings("rawtypes")
        final Class<? extends PropertyCopier> c = getClass();
        mapping = c.getAnnotation(Mapping.class);
        Validate.validState(mapping != null, "no @Mapping defined for %s", c);
        Validate.validState(mapping.value().length > 0, "@Mapping cannot be empty");

        for (Mapping.Value v : mapping.value()) {
            Validate.validState(StringUtils.isNotBlank(v.from()) || StringUtils.isNotBlank(v.to()),
                    "both from and to cannot be empty for a single @Mapping.Value");
        }
    }

    public boolean perform(TherianContext context, Copy<? extends SOURCE, ? extends TARGET> copy) {
        for (Mapping.Value v : mapping.value()) {
            Position.Readable<?> target = copy.getTargetPosition();
            final String to = StringUtils.trimToEmpty(v.to());
            if (!to.isEmpty()) {
                target = Property.at(to).of(target);
            }
            Position.Readable<?> source = copy.getSourcePosition();
            final String from = StringUtils.trimToEmpty(v.from());
            if (!from.isEmpty()) {
                source = Property.at(from).of(source);
            }
            final Copy<?, ?> nested = Copy.to(target, source);
            if (!context.evalSuccessIfSupported(nested)) {
                throw new OperationException(copy, "nested %s was unsuccessful", nested);
            }
        }
        return true;
    }

    @Override
    public boolean supports(TherianContext context, Copy<? extends SOURCE, ? extends TARGET> copy) {
        if (!super.supports(context, copy)) {
            return false;
        }
        for (Mapping.Value v : mapping.value()) {
            Position.Readable<?> target = copy.getTargetPosition();
            final String to = StringUtils.trimToEmpty(v.to());
            if (!to.isEmpty()) {
                target = Property.at(to).of(target);
            }
            Position.Readable<?> source = copy.getSourcePosition();
            final String from = StringUtils.trimToEmpty(v.from());
            if (!from.isEmpty()) {
                source = Property.at(from).of(source);
            }
            if (!context.supports(Copy.to(target, source))) {
                return false;
            }
        }
        return true;
    }
}
