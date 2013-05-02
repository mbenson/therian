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
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import therian.TherianModule;
import therian.TypeLiteral;
import therian.operation.Convert;
import therian.operator.OperatorTest;
import therian.util.Positions;

/**
 *
 */
public class IteratorToEnumerationTest extends OperatorTest {

    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new IteratorToEnumeration());
    }

    @Test
    public void test() {
        final List<String> l = Arrays.asList("foo", "bar", "baz");

        final Enumeration<String> enumeration =
            therianContext.eval(Convert.to(new TypeLiteral<Enumeration<String>>() {},
                Positions.readOnly(new TypeLiteral<Iterator<String>>() {}, l.iterator())));
        for (String s : l) {
            assertTrue(enumeration.hasMoreElements());
            assertEquals(s, enumeration.nextElement());
        }
        assertFalse(enumeration.hasMoreElements());
    }

}
