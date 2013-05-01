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

import java.util.Enumeration;
import java.util.Iterator;

import therian.TherianContext;
import therian.buildweaver.StandardOperator;
import therian.operation.Convert;
import therian.position.Ref;

/**
 * Implements simple conversion of a compatible {@link Enumeration} to an {@link Iterator}.
 */
@SuppressWarnings("rawtypes")
@StandardOperator
public class EnumerationToIterator extends ElementConverter<Enumeration<?>, Iterator> {
    public EnumerationToIterator() {
        super(Enumeration.class.getTypeParameters()[0], Iterator.class.getTypeParameters()[0]);
    }

    @Override
    public boolean perform(TherianContext context, Convert<? extends Enumeration<?>, ? super Iterator> operation) {
        final Iterable iterable = context.eval(Convert.to(Iterable.class, operation.getSourcePosition()));
        return context.forwardTo(Convert.to(Iterator.class, Ref.to(iterable)));
    }

}
