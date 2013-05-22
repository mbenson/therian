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

import java.util.Set;

import org.apache.commons.functor.UnaryFunction;
import org.apache.commons.functor.UnaryPredicate;
import org.apache.commons.functor.UnaryProcedure;
import org.apache.commons.functor.generator.FilteredGenerator;
import org.apache.commons.functor.generator.Generator;
import org.apache.commons.functor.generator.IteratorToGeneratorAdapter;
import org.apache.commons.functor.generator.TransformedGenerator;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;

import therian.Operator.DependsOn;
import therian.TherianContext;
import therian.operation.Copy;
import therian.operator.convert.DefaultCopyingConverter;
import therian.operator.convert.ELCoercionConverter;
import therian.operator.convert.NOPConverter;
import therian.position.Position;
import therian.position.relative.Property;
import therian.util.BeanProperties;

/**
 * Copies matching properties from source to target. Considered successful if one or more property conversions are
 * successful.
 */
@DependsOn({ ConvertingCopier.class, NOPConverter.class, ELCoercionConverter.class, DefaultCopyingConverter.class })
public abstract class AbstractBeanCopier<SOURCE, TARGET> extends Copier<SOURCE, TARGET> {

    public static final String[] IGNORED_PROPERTIES = { "class" };

    protected boolean ignoreProperty(String propertyName) {
        return ArrayUtils.contains(IGNORED_PROPERTIES, propertyName);
    }

    @Override
    public boolean supports(TherianContext context, Copy<? extends SOURCE, ? extends TARGET> copy) {
        return super.supports(context, copy)
            && !propertyCopyGenerator(context, copy.getSourcePosition(), copy.getTargetPosition()).toCollection()
                .isEmpty();
    }

    private Generator<Copy<?, ?>> propertyCopyGenerator(final TherianContext context,
        final Position.Readable<?> source, final Position.Readable<?> target) {

        final Set<String> sourceProperties =
            BeanProperties.getPropertyNames(BeanProperties.ReturnProperties.ALL, context, source);
        final Set<String> targetProperties =
            BeanProperties.getPropertyNames(BeanProperties.ReturnProperties.WRITABLE, context, target);
        targetProperties.retainAll(sourceProperties);

        final UnaryFunction<String, Copy<?, ?>> propertyNameToCopyOperation = new UnaryFunction<String, Copy<?, ?>>() {

            @Override
            public Copy<?, ?> evaluate(String name) {
                return ignoreProperty(name) ? null : Copy.Safely.to(Property.at(name).of(target),
                    Property.at(name).of(source));
            }
        };

        final UnaryPredicate<Copy<?, ?>> isSupported = new UnaryPredicate<Copy<?, ?>>() {

            @Override
            public boolean test(Copy<?, ?> copyOperation) {
                return copyOperation != null && context.supports(copyOperation);
            }
        };

        // adapt iterator over properties to generate copy operations and filter by those supported in the context:
        return new FilteredGenerator<Copy<?, ?>>(new TransformedGenerator<String, Copy<?, ?>>(
            IteratorToGeneratorAdapter.adapt(targetProperties.iterator()), propertyNameToCopyOperation), isSupported);
    }

    @Override
    public boolean perform(final TherianContext context, final Copy<? extends SOURCE, ? extends TARGET> copy) {
        final MutableBoolean result = new MutableBoolean();
        propertyCopyGenerator(context, copy.getSourcePosition(), copy.getTargetPosition()).run(
            new UnaryProcedure<Copy<?, ?>>() {

                @Override
                public void run(Copy<?, ?> propertyCopy) {
                    try {
                        if (context.evalSuccess(propertyCopy)) {
                            result.setValue(true);
                        }
                    } catch (Exception e) {
                        log.warn(propertyCopy.toString(), e);
                    }
                }
            });
        return result.booleanValue();
    }

}
