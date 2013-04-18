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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import therian.TherianContext;
import therian.operation.Convert;

/**
 * Implements simple conversion of a compatible {@link Iterator} to a {@link List}.
 */
@SuppressWarnings("rawtypes")
public class IteratorToList extends ElementConverter<Iterator<?>, List> {
    public IteratorToList() {
        super(Iterator.class.getTypeParameters()[0], List.class.getTypeParameters()[0]);
    }

    public boolean perform(TherianContext context, Convert<? extends Iterator<?>, ? super List> operation) {
        final List<Object> result = new ArrayList<Object>();
        for (Iterator<?> iter = operation.getSourcePosition().getValue(); iter != null && iter.hasNext();) {
            result.add(iter.next());
        }
        operation.getTargetPosition().setValue(result);
        return true;
    }

}
