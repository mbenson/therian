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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import therian.TherianModule;
import therian.TypeLiteral;
import therian.operation.Convert;
import therian.operator.OperatorTest;
import therian.testfixture.MetasyntacticVariable;
import therian.util.Positions;

public class NOPConverterTest extends OperatorTest {
    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new NOPConverter());
    }

    @Test
    public void test() {
        assertEquals("", therianContext.eval(Convert.to(String.class, Positions.readOnly(""))));
        assertEquals(MetasyntacticVariable.FOO,
            therianContext.eval(Convert.to(MetasyntacticVariable.class, Positions.readOnly(MetasyntacticVariable.FOO))));
        assertEquals(Long.valueOf(100L),
            therianContext.eval(Convert.to(Long.class, Positions.readOnly(Long.valueOf(100L)))));
        assertEquals(Boolean.TRUE, therianContext.eval(Convert.to(Boolean.class, Positions.readOnly(Boolean.TRUE))));

        final TypeLiteral<CharSequence> charSequenceType = new TypeLiteral<CharSequence>() {};
        assertNull(therianContext.eval(Convert.to(charSequenceType, Positions.readOnly(String.class, null))));
        assertEquals("", therianContext.eval(Convert.to(charSequenceType, Positions.readOnly(""))));
        assertEquals("", therianContext.eval(Convert.to(String.class, Positions.readOnly(""))));
        assertSame(MetasyntacticVariable.FOO,
            therianContext.eval(Convert.to(MetasyntacticVariable.class, Positions.readOnly(MetasyntacticVariable.FOO))));
        assertSame(MetasyntacticVariable.FOO, therianContext.eval(Convert.to(new TypeLiteral<Enum<?>>() {},
            Positions.readOnly(MetasyntacticVariable.FOO))));
        assertEquals(Integer.valueOf(666),
            therianContext.eval(Convert.to(Integer.class, Positions.readOnly(Integer.valueOf(666)))));
        assertEquals(Integer.valueOf(666),
            therianContext.eval(Convert.to(Number.class, Positions.readOnly(Integer.valueOf(666)))));

    }
}
