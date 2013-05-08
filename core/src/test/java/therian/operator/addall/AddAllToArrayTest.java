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
package therian.operator.addall;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import therian.OperationException;
import therian.TherianModule;
import therian.operation.AddAll;
import therian.operator.OperatorTest;
import therian.position.Position;
import therian.testfixture.MetasyntacticVariable;
import therian.util.Positions;

/**
 *
 */
public class AddAllToArrayTest extends OperatorTest {

    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new AddAllToArray());
    }

    @Test
    public void testEmptyTarget() {
        final Position.ReadWrite<MetasyntacticVariable[]> pos =
            Positions.readWrite(MetasyntacticVariable[].class, new MetasyntacticVariable[0]);
        assertTrue(therianContext.eval(AddAll.to(pos, Positions.readOnly(MetasyntacticVariable.values()))));
        assertArrayEquals(MetasyntacticVariable.values(), pos.getValue());
    }

    @Test
    public void testPopulatedTarget() {
        final Position.ReadWrite<MetasyntacticVariable[]> pos =
            Positions.readWrite(MetasyntacticVariable[].class,
                new MetasyntacticVariable[] { MetasyntacticVariable.FOO });
        assertTrue(therianContext.eval(AddAll.to(pos,
            Positions.readOnly(new MetasyntacticVariable[] { MetasyntacticVariable.BAR, MetasyntacticVariable.BAZ }))));
        assertArrayEquals(MetasyntacticVariable.values(), pos.getValue());
    }

    @Test(expected = OperationException.class)
    public void testMismatchedElementTypes() {
        therianContext.eval(AddAll.to(Positions.readWrite(String[].class, new String[0]),
            Positions.readOnly(new Object[0])));
    }

    @Test(expected = OperationException.class)
    public void testMismatchedElementTypesPrimitiveVsWrapper() {
        therianContext
            .eval(AddAll.to(Positions.readWrite(int[].class, new int[0]), Positions.readOnly(new Integer[0])));
    }

}
