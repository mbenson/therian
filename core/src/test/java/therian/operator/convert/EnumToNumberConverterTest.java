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

import org.junit.Test;

import therian.TherianModule;
import therian.operation.Convert;
import therian.operator.OperatorTest;
import therian.testfixture.MetasyntacticVariable;
import therian.util.Positions;

public class EnumToNumberConverterTest extends OperatorTest {

    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new EnumToNumberConverter());
    }

    @Test
    public void testConversion() {
        assertEquals(Double.valueOf(0.0),
            therianContext.eval(Convert.to(Double.class, Positions.readOnly(MetasyntacticVariable.FOO))));
        assertEquals(1, therianContext.eval(Convert.to(Integer.class, Positions.readOnly(MetasyntacticVariable.BAR)))
            .intValue());
        assertEquals(2L, therianContext.eval(Convert.to(Long.class, Positions.readOnly(MetasyntacticVariable.BAZ)))
            .longValue());
    }

}
