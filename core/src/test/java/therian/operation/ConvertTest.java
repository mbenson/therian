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
package therian.operation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;

import therian.OperationException;
import therian.Therian;
import therian.TherianContext;
import therian.TherianModule;
import therian.TypeLiteral;
import therian.operator.convert.NOPConverter;
import therian.operator.immutablecheck.DefaultImmutableChecker;
import therian.testfixture.MetasyntacticVariable;
import therian.util.Positions;

public class ConvertTest {
    private TherianContext therianContext;

    @Before
    public void setup() {
        therianContext =
            Therian.usingModules(
                TherianModule.create().withOperators(new NOPConverter(), new DefaultImmutableChecker())).context();
    }

    @Test(expected = OperationException.class)
    public void tesConvertUsingNullModules() {
        Therian therian = Therian.usingModules((TherianModule[]) null);
        TherianContext therianContext = therian.context();
        therianContext.eval(Convert.to(String.class, Positions.readOnly("")));
    }

    @Test
    public void testImmutableAssignable() {
        TypeLiteral<CharSequence> charSequenceType = new TypeLiteral<CharSequence>() {};
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
