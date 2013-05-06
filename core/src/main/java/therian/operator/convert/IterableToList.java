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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import therian.Operator.DependsOn;
import therian.TherianContext;
import therian.buildweaver.StandardOperator;
import therian.operation.Convert;
import therian.operation.GetElementType;
import therian.operator.getelementtype.GetIterableElementType;
import therian.util.Positions;
import therian.util.Types;

/**
 * Supports the rare case that an {@link Iterable} needs to be converted to a {@link List} or supertype (i.e.
 * {@link Collection}). Rejects noops.
 */
@SuppressWarnings("rawtypes")
@StandardOperator
@DependsOn({ GetIterableElementType.class, IteratorToList.class })
public class IterableToList extends AssignableElementConverter<Iterable<?>, List> {

    public IterableToList() {
        super(Iterable.class.getTypeParameters()[0], List.class.getTypeParameters()[0]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean perform(TherianContext context, Convert<? extends Iterable<?>, ? super List> convert) {
        final Type sourceElementType = context.eval(GetElementType.of(convert.getSourcePosition()));
        return context.forwardTo(Convert.to(
            convert.getTargetPosition(),
            Positions.readOnly(Types.parameterize(Iterator.class, sourceElementType), convert.getSourcePosition()
                .getValue().iterator())));
    }

}
