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

import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;

import therian.OperationException;
import therian.TherianModule;
import therian.TypeLiteral;
import therian.operation.Convert;
import therian.operator.OperatorTest;
import therian.testfixture.MetasyntacticVariable;
import therian.util.Positions;

public class MapToValuesTest extends OperatorTest {

    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new MapToValues());
    }

    @Test
    public void test() {
        assertTrue(therianContext.eval(Convert.to(Collection.class,
            Positions.readOnly(Collections.singletonMap("foo", MetasyntacticVariable.FOO)))) instanceof Collection<?>);
        assertTrue(therianContext.eval(Convert.to(Iterable.class,
            Positions.readOnly(Collections.singletonMap("foo", MetasyntacticVariable.FOO)))) instanceof Collection<?>);
        assertTrue(therianContext.eval(Convert.to(
            new TypeLiteral<Collection<MetasyntacticVariable>>() {},
            Positions.readOnly(new TypeLiteral<Map<String, MetasyntacticVariable>>() {},
                Collections.singletonMap("foo", MetasyntacticVariable.FOO)))) instanceof Collection<?>);
        assertTrue(therianContext.eval(Convert.to(
            new TypeLiteral<Iterable<MetasyntacticVariable>>() {},
            Positions.readOnly(new TypeLiteral<Map<String, MetasyntacticVariable>>() {},
                Collections.singletonMap("foo", MetasyntacticVariable.FOO)))) instanceof Collection<?>);
    }

    @Test
    public void testToIterable() {
        assertTrue(therianContext.eval(Convert.to(Iterable.class,
            Positions.readOnly(Collections.singletonMap("foo", MetasyntacticVariable.FOO)))) instanceof Collection<?>);
        assertTrue(therianContext.eval(Convert.to(
            new TypeLiteral<Iterable<MetasyntacticVariable>>() {},
            Positions.readOnly(new TypeLiteral<Map<String, MetasyntacticVariable>>() {},
                Collections.singletonMap("foo", MetasyntacticVariable.FOO)))) instanceof Collection<?>);
    }

    @Test(expected = OperationException.class)
    public void testUnknownSourceElementType() {
        therianContext.eval(Convert.to(new TypeLiteral<Collection<MetasyntacticVariable>>() {},
            Positions.readOnly(Collections.singletonMap("foo", MetasyntacticVariable.FOO))));
    }

    @Test(expected = OperationException.class)
    public void testIncompatibleElementType() {
        therianContext.eval(Convert.to(
            new TypeLiteral<Iterable<String>>() {},
            Positions.readOnly(new TypeLiteral<Map<String, MetasyntacticVariable>>() {},
                Collections.singletonMap("foo", MetasyntacticVariable.FOO))));
    }
}
