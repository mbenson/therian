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

import static therian.Operator.Phase.SUPPORT_CHECK;

import java.util.Enumeration;
import java.util.Iterator;

import therian.Operator.DependsOn;
import therian.Reusable;
import therian.TherianContext;
import therian.buildweaver.StandardOperator;
import therian.operation.Convert;
import therian.operator.getelementtype.GetEnumerationElementType;
import therian.operator.getelementtype.GetIteratorElementType;
import therian.position.Position;
import therian.util.Positions;

/**
 * Implements simple conversion of a compatible {@link Enumeration} to an {@link Iterator}.
 */
@SuppressWarnings("rawtypes")
@StandardOperator
@Reusable(SUPPORT_CHECK)
@DependsOn({ GetEnumerationElementType.class, GetIteratorElementType.class })
public class EnumerationToIterator extends AssignableElementConverter<Enumeration<?>, Iterator> {
    public EnumerationToIterator() {
        super(Enumeration.class.getTypeParameters()[0], Iterator.class.getTypeParameters()[0]);
    }

    @Override
    public boolean perform(TherianContext context, final Convert<? extends Enumeration<?>, ? super Iterator> operation) {
        final Position.ReadWrite<Iterable> rw = Positions.readWrite(Iterable.class);
        return context.evalSuccess(Convert.to(rw, operation.getSourcePosition()))
            && context.evalSuccess(Positions.writeValue(operation.getTargetPosition()), Convert.to(Iterator.class, rw));
    }
}
