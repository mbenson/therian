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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.junit.Test;

import therian.TherianModule;
import therian.operation.Convert;
import therian.operator.OperatorTest;
import therian.testfixture.MetasyntacticVariable;
import therian.util.Positions;

/**
 *
 */
public class DefaultToIteratorConverterTest extends OperatorTest {

    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new DefaultToIteratorConverter());
    }

    @Test
    public void testSingleton() {
        final Iterator<MetasyntacticVariable> result =
            therianContext.eval(Convert.to(new TypeLiteral<Iterator<MetasyntacticVariable>>() {},
                Positions.readOnly(MetasyntacticVariable.FOO)));
        assertTrue(result.hasNext());
        assertSame(MetasyntacticVariable.FOO, result.next());
        assertFalse(result.hasNext());
    }

    @Test
    public void testArray() {
        final Iterator<MetasyntacticVariable> result =
            therianContext.eval(Convert.to(new TypeLiteral<Iterator<MetasyntacticVariable>>() {},
                Positions.readOnly(MetasyntacticVariable.values())));
        for (MetasyntacticVariable foo : MetasyntacticVariable.values()) {
            assertTrue(result.hasNext());
            assertSame(foo, result.next());
        }
        assertFalse(result.hasNext());
    }

    @Test
    public void testPrimitiveArray() {
        final int[] beast = new int[] { 6, 6, 6 };
        final Iterator<Integer> result =
            therianContext.eval(Convert.to(new TypeLiteral<Iterator<Integer>>() {}, Positions.readOnly(beast)));
        for (int i = 0; i < beast.length; i++) {
            assertTrue(result.hasNext());
            assertEquals(beast[i], result.next().intValue());
        }
        assertFalse(result.hasNext());
    }
}
