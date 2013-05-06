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

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import therian.OperationException;
import therian.TherianModule;
import therian.TypeLiteral;
import therian.operation.Convert;
import therian.operator.OperatorTest;
import therian.operator.getelementtype.GetIterableElementType;
import therian.util.Positions;

/**
 *
 */
public class IterableToListTest extends OperatorTest {

    public static class Foo implements Iterable<String> {

        /**
         * {@inheritDoc}
         */
        @Override
        public Iterator<String> iterator() {
            return Arrays.asList("foo", "bar", "baz").iterator();
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new IterableToList(), new IteratorToList(),
            new GetIterableElementType());
    }

    @Test
    public void test() {
        assertEquals(Arrays.asList("foo", "bar", "baz"),
            therianContext.eval(Convert.to(new TypeLiteral<List<String>>() {}, Positions.readOnly(new Foo()))));
        assertEquals(Arrays.asList("foo", "bar", "baz"),
            therianContext.eval(Convert.to(new TypeLiteral<Collection<String>>() {}, Positions.readOnly(new Foo()))));
    }

    @Test(expected = OperationException.class)
    public void testIgnoreToIterableNoop() {
        therianContext.eval(Convert.to(Iterable.class, Positions.readOnly(new Foo())));
    }

    @Test(expected = OperationException.class)
    public void testIgnoreListToIterableNoop() {
        therianContext.eval(Convert.to(Iterable.class, Positions.readOnly(Arrays.asList("whatever"))));
    }
}
