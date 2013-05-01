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
import java.util.Set;

import org.junit.Test;

import therian.TherianModule;
import therian.operation.Convert;
import therian.operator.OperatorTest;
import therian.operator.getelementtype.GetArrayElementType;
import therian.position.Ref;

public class CollectionToArrayTest extends OperatorTest {

    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new CollectionToArray(), new GetArrayElementType());
    }

    @Test
    public void test() {
        final String[] array = { "foo", "bar", "baz" };
        assertArrayEquals(array, therianContext.eval(Convert.to(String[].class, Ref.to(Arrays.asList(array)))));
    }

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
            therianContext.eval(Convert.to(int[].class, new Ref<Set<Integer>>(s) {})));
    }
}
