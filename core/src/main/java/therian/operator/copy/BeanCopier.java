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

import java.beans.FeatureDescriptor;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.functor.UnaryFunction;
import org.apache.commons.functor.UnaryPredicate;
import org.apache.commons.functor.UnaryProcedure;
import org.apache.commons.functor.generator.FilteredGenerator;
import org.apache.commons.functor.generator.Generator;
import org.apache.commons.functor.generator.IteratorToGeneratorAdapter;
import org.apache.commons.functor.generator.TransformedGenerator;
import org.apache.commons.lang3.ArrayUtils;

import therian.TherianContext;
import therian.operation.Copy;
import therian.position.Position;
import therian.position.relative.Property;

/**
 * Copies matching properties from source to target. Considered successful if one or more property conversions are
 * successful.
 */
public class BeanCopier extends Copier<Object, Object> {
    public static final String[] SKIP_PROPERTIES = { "class" };

    public void perform(final Copy<?, ?> copy) {
        final TherianContext context = TherianContext.getRequiredInstance();

        propertyCopyGenerator(copy.getSourcePosition(), copy.getTargetPosition()).run(new UnaryProcedure<Copy<?, ?>>() {

            public void run(Copy<?, ?> propertyCopy) {
                context.eval(propertyCopy);
                if (propertyCopy.isSuccessful()) {
                    copy.setSuccessful(true);
                }
            }
        });
    }

    @Override
    public boolean supports(Copy<?, ?> copy) {
        return super.supports(copy)
            && !propertyCopyGenerator(copy.getSourcePosition(), copy.getTargetPosition()).toCollection().isEmpty();
    }

    private Generator<Copy<?, ?>> propertyCopyGenerator(final Position.Readable<?> source,
        final Position.Readable<?> target) {
        final TherianContext context = TherianContext.getInstance();

        final Set<String> sourceProperties = getPropertyNames(context, source);
        final Set<String> targetProperties = getPropertyNames(context, target);
        targetProperties.retainAll(sourceProperties);

        return new FilteredGenerator<Copy<?, ?>>(new TransformedGenerator<String, Copy<?, ?>>(
            IteratorToGeneratorAdapter.adapt(targetProperties.iterator()), new UnaryFunction<String, Copy<?, ?>>() {

                public Copy<?, ?> evaluate(String name) {
                    return Copy.Safely.to(Property.at(name).of(target), Property.at(name).of(source));
                }
            }), new UnaryPredicate<Copy<?, ?>>() {

            public boolean test(Copy<?, ?> obj) {
                return context.supports(obj);
            }
        });
    }

    private Set<String> getPropertyNames(TherianContext context, Position.Readable<?> position) {
        final HashSet<String> result = new HashSet<String>();
        for (Iterator<FeatureDescriptor> iter =
            context.getELResolver().getFeatureDescriptors(context, position.getValue()); iter.hasNext();) {
            final String name = iter.next().getName();
            if (!ArrayUtils.contains(SKIP_PROPERTIES, name)) {
                result.add(name);
            }
        }
        return result;
    }

}
