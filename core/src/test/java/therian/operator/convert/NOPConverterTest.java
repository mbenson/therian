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

import org.junit.Before;
import org.junit.Test;

import therian.Therian;
import therian.TherianContext;
import therian.TherianModule;
import therian.operation.Convert;
import therian.operator.immutablecheck.DefaultImmutableChecker;
import therian.position.Ref;
import therian.testfixture.MetasyntacticVariable;

public class NOPConverterTest {
    private TherianContext context;

    @Before
    public void setup() {
        context =
            Therian.usingModules(
                TherianModule.create().withOperators(new NOPConverter(), new DefaultImmutableChecker())).context();
    }

    @Test
    public void test() {
        assertEquals("", context.eval(Convert.to(String.class, Ref.to(""))));
        assertEquals(MetasyntacticVariable.FOO,
            context.eval(Convert.to(MetasyntacticVariable.class, Ref.to(MetasyntacticVariable.FOO))));
        assertEquals(Long.valueOf(100L), context.eval(Convert.to(Long.class, Ref.to(Long.valueOf(100L)))));
        assertEquals(Boolean.TRUE, context.eval(Convert.to(Boolean.class, Ref.to(Boolean.TRUE))));
    }

}
