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

/**
 * Implements simple conversion of a compatible {@link Iterator} to an {@link Enumeration}.
 */
@SuppressWarnings("rawtypes")
@StandardOperator
@Reusable(SUPPORT_CHECK)
@DependsOn({ GetIteratorElementType.class, GetEnumerationElementType.class })
public class IteratorToEnumeration extends AssignableElementConverter<Iterator<?>, Enumeration> {
    public IteratorToEnumeration() {
        super(Iterator.class.getTypeParameters()[0], Enumeration.class.getTypeParameters()[0]);
    }

    @Override
    public boolean perform(TherianContext context, Convert<? extends Iterator<?>, ? super Enumeration> operation) {
        final Iterator<?> iter = operation.getSourcePosition().getValue();

        operation.getTargetPosition().setValue(new Enumeration<Object>() {

            @Override
            public boolean hasMoreElements() {
                return iter.hasNext();
            }

            @Override
            public Object nextElement() {
                return iter.next();
            }
        });
        return true;
    }
}
