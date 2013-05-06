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
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Iterator;

import org.junit.Test;

import therian.OperationException;
import therian.TherianModule;
import therian.operation.Convert;
import therian.operator.OperatorTest;
import therian.util.Positions;

/**
 *
 */
public class DefaultToArrayConverterTest extends OperatorTest {

    public static class Foo implements Iterable<String> {

        /**
         * {@inheritDoc}
         */
        @Override
        public Iterator<String> iterator() {
            return Arrays.asList(DefaultToArrayConverterTest.STRINGS).iterator();
        }

    }

    private static final String[] STRINGS = new String[] { "foo", "bar", "baz" };

    // * <li>in the worst case, converts from source to {@link Iterable} to {@link Collection} to target</li>
    protected TherianModule module() {
        return TherianModule.create().withOperators(new DefaultToArrayConverter(), new DefaultToListConverter());
    }

    @Test(expected = OperationException.class)
    public void testArrayOfStringToArrayOfString() {
        therianContext.eval(Convert.to(String[].class, Positions.readOnly(new String[] { "foo" })));
    }

    @Test(expected = OperationException.class)
    public void testArrayOfStringToArrayOfObject() {
        therianContext.eval(Convert.to(Object[].class, Positions.readOnly(new String[] { "foo" })));
    }

    @Test
    public void testStringToArrayOfString() {
        assertArrayEquals(new String[] { "foo" },
            therianContext.eval(Convert.to(String[].class, Positions.readOnly("foo"))));
    }

//    @Test
    public void testStringToArrayOfObject() {
        fail("Not yet implemented");
    }

//    @Test(expected = OperationException.class)
    public void testArrayOfIntToArrayOfInt() {
        fail("Not yet implemented");
    }

//    @Test
    public void testArrayOfIntToArrayOfInteger() {
        fail("Not yet implemented");
    }

//    @Test
    public void testArrayOfIntToArrayOfObject() {
        fail("Not yet implemented");
    }

//    @Test
    public void testIntToArrayOfInt() {
        fail("Not yet implemented");
    }

//    @Test
    public void testIterableOfStringToArrayOfString() {
        fail("Not yet implemented");
    }

}
