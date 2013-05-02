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

import org.apache.commons.functor.UnaryProcedure;

import therian.TherianContext;
import therian.buildweaver.StandardOperator;
import therian.operation.Convert;
import therian.position.Box;

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
    public boolean perform(TherianContext context, final Convert<? extends Enumeration<?>, ? super Iterator> operation) {
        final Box<Iterable> iterable = new Box<Iterable>(Iterable.class);
        return context.evalSuccess(Convert.to(iterable, operation.getSourcePosition()))
            && context.forwardTo(Convert.to(Iterator.class, iterable), new UnaryProcedure<Iterator>() {

                @Override
                public void run(Iterator iter) {
                    operation.getTargetPosition().setValue(iter);
                }
            });
    }

}
