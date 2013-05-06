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

import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import therian.TherianModule;
import therian.TypeLiteral;
import therian.operation.Convert;
import therian.operator.OperatorTest;
import therian.util.Positions;

public class CollectionToArrayTest extends OperatorTest {

    private static final String[] STRINGS = { "foo", "bar", "baz" };

    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new CollectionToArray());
    }

    @Test
    public void testListOfStringToArrayOfString() {
        assertArrayEquals(
            STRINGS,
            therianContext.eval(Convert.to(String[].class,
                Positions.readOnly(new TypeLiteral<List<String>>() {}, Arrays.asList(STRINGS)))));
    }

    @Test
    public void testListOfStringToArrayOfObject() {
        new TypeLiteral<List<String>>() {};
        assertArrayEquals(
            STRINGS,
            therianContext.eval(Convert.to(Object[].class,
                Positions.readOnly(new TypeLiteral<List<String>>() {}, Arrays.asList(STRINGS)))));
    }

    @Test
    public void testPrimitive() {
        @SuppressWarnings("serial")
        final Set<Integer> s = new LinkedHashSet<Integer>() {
            {
                for (int i = 0; i < 3; i++) {
                    add(Integer.valueOf(i));
                }
            }
        };
        assertArrayEquals(new int[] { 0, 1, 2 },
            therianContext.eval(Convert.to(int[].class, Positions.readOnly(new TypeLiteral<Set<Integer>>() {}, s))));
    }
}
