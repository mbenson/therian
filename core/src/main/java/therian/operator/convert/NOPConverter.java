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

import therian.Operator.Phase;
import therian.Hint;
import therian.Operators;
import therian.Reusable;
import therian.TherianContext;
import therian.buildweaver.StandardOperator;
import therian.operation.Convert;

/**
 * Uses source value as target value when assignable. This is a so-called "standard converter," but a more strongly
 * typed Converter can be specified for any given datatype that requires a new instance. Some type of
 * {@link CopyingConverter} is recommended for this purpose.
 *
 * @see Operators#standard()
 */
@Reusable(Phase.SUPPORT_CHECK)
// simpler to reinvoke than store value
@StandardOperator
public class NOPConverter extends Converter.WithDynamicTarget<Object> {
    /**
     * NullBehavior {@link Hint}
     */
    public enum NullBehavior implements Hint {
        /**
         * Indicates that a {@code null} source value will be considered unsupported.
         */
        UNSUPPORTED,

        /**
         * Indicates that a {@code null} source value will be considered supported, but do nothing.
         */
        NOP,

        /**
         * Indicates that a {@code null} source value will yield a {@code null} target value.
         */
        DEFAULT;

        @Override
        public Class<? extends Hint> getType() {
            return NullBehavior.class;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean perform(TherianContext context, Convert<?, ?> convert) {
        if (convert.getSourcePosition().getValue() == null
            && context.getTypedContext(NullBehavior.class) == NullBehavior.UNSUPPORTED) {
            return false;
        }
        @SuppressWarnings("rawtypes")
        final Convert raw = convert;
        raw.getTargetPosition().setValue(raw.getSourcePosition().getValue());
        return true;
    }

    @Override
    public boolean supports(TherianContext context, Convert<?, ?> convert) {
        return isNoop(convert)
            && (convert.getSourcePosition().getValue() != null || context.getTypedContext(NullBehavior.class,
                NullBehavior.DEFAULT) == NullBehavior.DEFAULT);
    }
}
