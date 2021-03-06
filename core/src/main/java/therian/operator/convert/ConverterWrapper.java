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

import therian.Operator;
import therian.TherianContext;
import therian.operation.Convert;

/**
 * Provides a means of narrowing the type parameters of a given converter.
 *
 * @param <SOURCE>
 * @param <TARGET>
 */
public abstract class ConverterWrapper<SOURCE, TARGET> extends Converter<SOURCE, TARGET> {
    @SuppressWarnings("rawtypes")
    private final Operator wrapped;

    protected ConverterWrapper(Operator<Convert<? super SOURCE, ? extends TARGET>> wrapped) {
        super();
        this.wrapped = wrapped;
    }

    @SuppressWarnings("unchecked")
    public boolean perform(TherianContext context, Convert<? extends SOURCE, ? super TARGET> operation) {
        return wrapped.perform(context, operation);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean supports(TherianContext context, Convert<? extends SOURCE, ? super TARGET> convert) {
        return super.supports(context, convert) && convert.matches(wrapped) && wrapped.supports(context, convert);
    }
}
