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
package therian.operator.add;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import therian.OperationException;
import therian.TherianModule;
import therian.operation.Add;
import therian.operator.OperatorTest;
import therian.testfixture.MetasyntacticVariable;
import therian.util.Positions;

/**
 *
 */
public class AddEntryToMapTest extends OperatorTest {

    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new AddEntryToMap());
    }

    @Test
    public void testRawMap() {
        @SuppressWarnings("rawtypes")
        final Map m = new HashMap();
        assertTrue(therianContext.eval(
            Add.to(Positions.readWrite(Map.class, m),
                Positions.readOnly(Pair.of(MetasyntacticVariable.FOO.name(), MetasyntacticVariable.FOO))))
            .booleanValue());
        assertEquals(1, m.size());
        assertEquals(Pair.of(MetasyntacticVariable.FOO.name(), MetasyntacticVariable.FOO), m.entrySet().iterator()
            .next());
    }

    @Test
    public void testTypedMap() {
        final Map<String, MetasyntacticVariable> m = new HashMap<>();
        assertTrue(therianContext.eval(
            Add.to(Positions.readWrite(new TypeLiteral<Map<String, MetasyntacticVariable>>() {}, m),
                Positions.readOnly(Pair.of(MetasyntacticVariable.FOO.name(), MetasyntacticVariable.FOO))))
            .booleanValue());
        assertEquals(1, m.size());
        assertEquals(Pair.of(MetasyntacticVariable.FOO.name(), MetasyntacticVariable.FOO), m.entrySet().iterator()
            .next());
    }

    @Test(expected = OperationException.class)
    public void testImmutableMap() {
        therianContext.eval(Add.to(
            Positions.readWrite(new TypeLiteral<Map<String, MetasyntacticVariable>>() {},
                Collections.singletonMap(MetasyntacticVariable.FOO.name(), MetasyntacticVariable.FOO)),
            Positions.readOnly(Pair.of(MetasyntacticVariable.BAR.name(), MetasyntacticVariable.BAR))));
    }

    @Test(expected = OperationException.class)
    public void testWrongKeyTypeMap() {
        therianContext
            .eval(Add.to(
                Positions.readWrite(new TypeLiteral<Map<String, String>>() {}, new HashMap<>()),
                Positions.readOnly(new TypeLiteral<Map.Entry<Integer, String>>() {},
                    Pair.of(Integer.valueOf(666), "foo"))));
    }

    @Test(expected = OperationException.class)
    public void testWrongValueTypeMap() {
        therianContext.eval(Add.to(Positions.readWrite(new TypeLiteral<Map<String, String>>() {}, new HashMap<>()),
            Positions.readOnly(new TypeLiteral<Map.Entry<String, Object>>() {}, Pair.of("foo", new Object()))));
    }

    @Test(expected = OperationException.class)
    public void testIncompatibleKeyMap() {
        therianContext.eval(Add.to(Positions.readWrite(new TypeLiteral<Map<String, String>>() {}, new HashMap<>()),
            Positions.readOnly(Pair.of(Integer.valueOf(666), "foo"))));
    }

    @Test(expected = OperationException.class)
    public void testIncompatibleValueMap() {
        therianContext.eval(Add.to(Positions.readWrite(new TypeLiteral<Map<String, String>>() {}, new HashMap<>()),
            Positions.readOnly(Pair.of("foo", new Object()))));
    }

    @Test(expected = OperationException.class)
    public void testAddNullEntry() {
        final Map<String, MetasyntacticVariable> m = new HashMap<>();
        therianContext.eval(Add.to(Positions.readWrite(new TypeLiteral<Map<String, MetasyntacticVariable>>() {}, m),
            Positions.readOnly(new TypeLiteral<Map.Entry<String, MetasyntacticVariable>>() {}, (Map.Entry<String, MetasyntacticVariable>) null)));
    }
}
