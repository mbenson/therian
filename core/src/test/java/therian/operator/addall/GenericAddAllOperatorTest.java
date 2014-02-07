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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.junit.Test;

import therian.TherianModule;
import therian.operation.AddAll;
import therian.operator.OperatorTest;
import therian.position.Position;
import therian.testfixture.MetasyntacticVariable;
import therian.util.Positions;

public class GenericAddAllOperatorTest extends OperatorTest {

    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new GenericAddAllOperator());
    }

    @Test
    public void testAddObjectToList() {
        final List<String> targetList = new ArrayList<String>();
        final Position.Readable<List<String>> target =
            Positions.readOnly(new TypeLiteral<List<String>>() {}, targetList);

        assertTrue(therianContext.eval(AddAll.to(target, Positions.readOnly("foo"))).booleanValue());

        assertEquals(1, targetList.size());
        assertEquals("foo", targetList.get(0));
    }

    @Test
    public void testAddObjectToListIterator() {
        final List<String> targetList = new ArrayList<String>();
        final Position.Readable<ListIterator<String>> target =
            Positions.readOnly(new TypeLiteral<ListIterator<String>>() {}, targetList.listIterator());

        assertTrue(therianContext.eval(AddAll.to(target, Positions.readOnly("foo"))).booleanValue());

        assertEquals(1, targetList.size());
        assertEquals("foo", targetList.get(0));
    }

    @Test
    public void testReaddObjectToSet() {
        final Set<MetasyntacticVariable> targetSet =
            new LinkedHashSet<MetasyntacticVariable>(Arrays.asList(MetasyntacticVariable.values()));
        final Position.Readable<Set<MetasyntacticVariable>> target =
            Positions.readOnly(new TypeLiteral<Set<MetasyntacticVariable>>() {}, targetSet);

        assertFalse(therianContext.eval(AddAll.to(target, Positions.readOnly(MetasyntacticVariable.FOO)))
            .booleanValue());

        assertArrayEquals(MetasyntacticVariable.values(), targetSet.toArray());
    }

    @Test
    public void testMergeIntoSet() {
        final MetasyntacticVariable[] mv = MetasyntacticVariable.values();
        final MetasyntacticVariable[] sourceArray = new MetasyntacticVariable[mv.length * 2];
        System.arraycopy(mv, 0, sourceArray, 0, mv.length);
        System.arraycopy(mv, 0, sourceArray, mv.length, mv.length);

        final Set<MetasyntacticVariable> targetSet = new LinkedHashSet<MetasyntacticVariable>();
        final Position.Readable<Set<MetasyntacticVariable>> target =
            Positions.readOnly(new TypeLiteral<Set<MetasyntacticVariable>>() {}, targetSet);

        assertTrue(therianContext.eval(AddAll.to(target, Positions.readOnly(sourceArray))));

        assertArrayEquals(mv, targetSet.toArray());
    }
}
