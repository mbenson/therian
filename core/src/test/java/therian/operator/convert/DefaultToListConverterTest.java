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
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.TypeLiteral;
import org.junit.Test;

import therian.TherianModule;
import therian.operation.Convert;
import therian.operator.OperatorTest;
import therian.testfixture.MetasyntacticVariable;
import therian.util.Positions;

public class DefaultToListConverterTest extends OperatorTest {

    private static final TypeLiteral<List<MetasyntacticVariable>> LIST_OF_MV_TYPE =
        new TypeLiteral<List<MetasyntacticVariable>>() {};

    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new DefaultToListConverter());
    }

    @Test
    public void testSingleton() {
        assertEquals(Collections.singletonList(MetasyntacticVariable.FOO),
            therianContext.eval(Convert.to(LIST_OF_MV_TYPE, Positions.readOnly(MetasyntacticVariable.FOO))));
    }

    @Test
    public void testArray() {
        assertEquals(Arrays.asList(MetasyntacticVariable.values()),
            therianContext.eval(Convert.to(LIST_OF_MV_TYPE, Positions.readOnly(MetasyntacticVariable.values()))));
        assertEquals(Arrays.asList(),
            therianContext.eval(Convert.to(LIST_OF_MV_TYPE, Positions.readOnly(new MetasyntacticVariable[0]))));
    }

    @Test
    public void testPrimitiveArray() {
        final int[] beast = { 6, 6, 6 };
        assertEquals(Arrays.asList(ArrayUtils.toObject(beast)),
            therianContext.eval(Convert.to(new TypeLiteral<List<Integer>>() {}, Positions.readOnly(beast))));
    }
}
