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
 * Allows chaining of converters:
 * 
 * <pre>
 * CompositeConverter.taking(
 * </pre>
 */
public class CompositeConverter implements Operator<Convert<?, ?>> {
    private final Operator<Convert<?, ?>> preceding;
    private final Operator<Convert<?, ?>> following;

    private CompositeConverter(Operator<Convert<?, ?>> preceding, Operator<Convert<?, ?>> following) {
        super();
        // TODO validate assignability from one to the next
        this.preceding = preceding;
        this.following = following;
    }

    public void perform(TherianContext context, Convert<?, ?> operation) {
        Convert<?, ?> convert;
        if (preceding == null) {
            convert = operation;
        } else {
            // TODO
            convert = null;
        }
        following.perform(null, convert);

        if (convert != operation) {
            operation.setSuccessful(convert.isSuccessful());
        }
    }

    public boolean supports(TherianContext context, Convert<?, ?> operation) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public CompositeConverter from(Operator<Convert<?, ?>> preceding) {
        return new CompositeConverter(preceding, this);
    }

    public static CompositeConverter taking(Operator<Convert<?, ?>> delegate) {
        return new CompositeConverter(null, delegate);
    }
}
