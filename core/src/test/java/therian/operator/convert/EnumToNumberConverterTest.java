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

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import therian.TherianModule;
import therian.operation.Convert;
import therian.operator.TransformerTest;
import therian.position.Ref;
import therian.testfixture.MetasyntacticVariable;

public class EnumToNumberConverterTest extends TransformerTest {

    @Override
    protected TherianModule[] modules() {
        return ArrayUtils.toArray(TherianModule.create().withOperators(new EnumToNumberConverter(),
            new ELCoercionConverter()));
    }

    @Test
    public void testConversion() {
        assertEquals(Double.valueOf(0.0),
            therianContext.eval(Convert.to(Double.class, Ref.to(MetasyntacticVariable.FOO))));
        assertEquals(1, therianContext.eval(Convert.to(Integer.class, Ref.to(MetasyntacticVariable.BAR))).intValue());
        assertEquals(2L, therianContext.eval(Convert.to(Long.class, Ref.to(MetasyntacticVariable.BAZ))).longValue());
    }

}
