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

import therian.Operator.DependsOn;
import therian.TherianContext;
import therian.buildweaver.StandardOperator;
import therian.operation.Convert;
import therian.operator.getelementtype.GetEnumerationElementType;
import therian.operator.getelementtype.GetIterableElementType;
import therian.util.Positions;

/**
 * Implements simple conversion of a compatible {@link Iterable} to an {@link Enumeration}.
 */
@SuppressWarnings("rawtypes")
@StandardOperator
@DependsOn({ GetIterableElementType.class, GetEnumerationElementType.class, IteratorToEnumeration.class })
public class IterableToEnumeration extends AssignableElementConverter<Iterable<?>, Enumeration> {
    public IterableToEnumeration() {
        super(Iterable.class.getTypeParameters()[0], Enumeration.class.getTypeParameters()[0]);
    }

    @Override
    public boolean perform(TherianContext context, final Convert<? extends Iterable<?>, ? super Enumeration> convert) {
        return context
            .evalSuccess(Positions.writeValue(convert.getTargetPosition()),
                Convert.to(Enumeration.class, Positions.readOnly(convert.getSourcePosition().getValue().iterator())));
    }
}
