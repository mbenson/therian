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

import java.util.Enumeration;

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
public class DefaultToEnumerationConverterTest extends OperatorTest {

    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new DefaultToEnumerationConverter());
    }

    @Test
    public void testSingleton() {
        final Enumeration<MetasyntacticVariable> result =
            therianContext.eval(Convert.to(new TypeLiteral<Enumeration<MetasyntacticVariable>>() {},
                Positions.readOnly(MetasyntacticVariable.FOO)));
        assertTrue(result.hasMoreElements());
        assertSame(MetasyntacticVariable.FOO, result.nextElement());
        assertFalse(result.hasMoreElements());
    }

    @Test
    public void testArray() {
        final Enumeration<MetasyntacticVariable> result =
            therianContext.eval(Convert.to(new TypeLiteral<Enumeration<MetasyntacticVariable>>() {},
                Positions.readOnly(MetasyntacticVariable.values())));
        for (MetasyntacticVariable foo : MetasyntacticVariable.values()) {
            assertTrue(result.hasMoreElements());
            assertSame(foo, result.nextElement());
        }
        assertFalse(result.hasMoreElements());
    }

    @Test
    public void testPrimitiveArray() {
        final int[] beast = new int[] { 6, 6, 6 };
        final Enumeration<Integer> result =
            therianContext.eval(Convert.to(new TypeLiteral<Enumeration<Integer>>() {}, Positions.readOnly(beast)));
        for (int i = 0; i < beast.length; i++) {
            assertTrue(result.hasMoreElements());
            assertEquals(beast[i], result.nextElement().intValue());
        }
        assertFalse(result.hasMoreElements());
    }
}
