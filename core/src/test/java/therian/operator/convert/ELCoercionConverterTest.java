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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import therian.TherianModule;
import therian.operation.Convert;
import therian.operator.OperatorTest;
import therian.testfixture.MetasyntacticVariable;
import therian.util.Positions;

public class ELCoercionConverterTest extends OperatorTest {

    @Override
    protected TherianModule[] modules() {
        return new TherianModule[] { TherianModule.create().withOperators(new ELCoercionConverter()) };
    }

    @Test
    public void testCoerciontoString() {
        assertEquals("666", therianContext.eval(Convert.to(String.class, Positions.readOnly(Integer.valueOf(666)))));
    }

    @Test
    public void testCoerciontoEnum() {
        assertNull(therianContext.eval(Convert.to(MetasyntacticVariable.class, Positions.readOnly(Object.class, null))));
        assertNull(therianContext.eval(Convert.to(MetasyntacticVariable.class, Positions.readOnly(""))));
        assertSame(MetasyntacticVariable.FOO,
            therianContext.eval(Convert.to(MetasyntacticVariable.class, Positions.readOnly("FOO"))));
    }

    @Test
    public void testCoercionToBoolean() {
        assertFalse(therianContext.eval(
            Convert.<Object, Boolean> to(Boolean.class, Positions.readOnly(Object.class, null))).booleanValue());
        assertFalse(therianContext.eval(Convert.<String, Boolean> to(Boolean.class, Positions.readOnly("")))
            .booleanValue());
        assertFalse(therianContext.eval(Convert.<String, Boolean> to(Boolean.class, Positions.readOnly("false")))
            .booleanValue());
        assertFalse(therianContext.eval(Convert.<String, Boolean> to(Boolean.class, Positions.readOnly("whatever")))
            .booleanValue());
        assertTrue(therianContext.eval(Convert.<String, Boolean> to(Boolean.class, Positions.readOnly("true")))
            .booleanValue());
    }

}
