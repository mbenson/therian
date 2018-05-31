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
package therian.operator.convert;

import java.lang.reflect.Type;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import therian.Operator;
import therian.TherianContext;
import therian.operation.Convert;
import therian.operator.FromSourceToTarget;

/**
 * {@link Convert} {@link Operator} superclass.
 *
 * Note the assignability constraints:
 * <ul>
 * <li>SOURCE assignable from source type</li>
 * <li>TARGET assignable to target type</li>
 * </ul>
 *
 * For example, if you wanted to convert a {@link String} to a {@link Number}, then a {@link Converter} of
 * {@link CharSequence} to {@link Integer} would satisfy. The inverse is not necessarily true: a {@link Converter}
 * declared to convert {@link String} to {@link Number} may not be able to handle the source value of, or produce a
 * target value compatible with, a conversion requested from {@link CharSequence} to {@link Integer}. Thus it is best to
 * define your domain APIs as widely as possible, and for your {@link Converter} implementations, parameterize SOURCE as
 * widely as possible and TARGET as narrowly as possible.
 *
 * @param <SOURCE>
 * @param <TARGET>
 */
public abstract class Converter<SOURCE, TARGET> extends AbstractConverter<Convert<? extends SOURCE, ? super TARGET>>
    implements FromSourceToTarget.FromSource<SOURCE>, FromSourceToTarget.ToTarget<TARGET> {

    /**
     * Accommodates the situation where a {@link Converter} applies to multiple target types that share no common
     * descendant as in the case of e.g. array types.
     */
    public static abstract class WithDynamicTarget<SOURCE> extends AbstractConverter<Convert<? extends SOURCE, ?>>
        implements FromSourceToTarget.FromSource<SOURCE> {

        /**
         * {@link Logger} instance.
         */
        protected final Logger log = LoggerFactory.getLogger(getClass());

        // override default parameter name
        @Override
        public abstract boolean perform(TherianContext context, Convert<? extends SOURCE, ?> convert);

        @Override
        public boolean supports(TherianContext context, Convert<? extends SOURCE, ?> convert) {
            return !(isNoop(convert) && isRejectNoop())
                && TypeUtils.isInstance(convert.getSourcePosition().getValue(), getSourceBound());
        }

        @Override
        protected Type getTargetBound() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * {@link Logger} instance.
     */
    protected final Logger log = LoggerFactory.getLogger(getClass());

    // override default parameter name
    @Override
    public abstract boolean perform(TherianContext context, Convert<? extends SOURCE, ? super TARGET> convert);

    /**
     * {@inheritDoc}
     *
     * @return {@code true} if the source value is an instance of our SOURCE bound and the target type is assignable
     *         from our TARGET bound. If, the source value is an instance of the target type, returns !
     *         {@link #isRejectNoop()}.
     */
    @Override
    public boolean supports(TherianContext context, Convert<? extends SOURCE, ? super TARGET> convert) {
        return !(isNoop(convert) && isRejectNoop())
            && TypeUtils.isAssignable(convert.getSourceType().getType(), getSourceBound())
            && TypeUtils.isAssignable(getTargetBound(), convert.getTargetType().getType());
    }
}
