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

import org.junit.Before;
import org.junit.Test;

import therian.OperationException;
import therian.Therian;
import therian.TherianContext;
import therian.TherianModule;
import therian.TypeLiteral;
import therian.operation.Convert;
import therian.testfixture.MetasyntacticVariable;
import therian.util.Positions;

public class MapToValuesTest {
    private TherianContext context;

    @Before
    public void setup() {
        context = Therian.usingModules(TherianModule.create().withOperators(new MapToValues())).context();
    }

    @Test
    public void test() {
        assertTrue(context.eval(Convert.to(Collection.class,
            Positions.readOnly(Collections.singletonMap("foo", MetasyntacticVariable.FOO)))) instanceof Collection<?>);
        assertTrue(context.eval(Convert.to(Iterable.class,
            Positions.readOnly(Collections.singletonMap("foo", MetasyntacticVariable.FOO)))) instanceof Collection<?>);
        assertTrue(context.eval(Convert.to(
            new TypeLiteral<Collection<MetasyntacticVariable>>() {},
            Positions.readOnly(new TypeLiteral<Map<String, MetasyntacticVariable>>() {},
                Collections.singletonMap("foo", MetasyntacticVariable.FOO)))) instanceof Collection<?>);
        assertTrue(context.eval(Convert.to(
            new TypeLiteral<Iterable<MetasyntacticVariable>>() {},
            Positions.readOnly(new TypeLiteral<Map<String, MetasyntacticVariable>>() {},
                Collections.singletonMap("foo", MetasyntacticVariable.FOO)))) instanceof Collection<?>);
    }

    @Test
    public void testToIterable() {
        assertTrue(context.eval(Convert.to(Iterable.class,
            Positions.readOnly(Collections.singletonMap("foo", MetasyntacticVariable.FOO)))) instanceof Collection<?>);
        assertTrue(context.eval(Convert.to(
            new TypeLiteral<Iterable<MetasyntacticVariable>>() {},
            Positions.readOnly(new TypeLiteral<Map<String, MetasyntacticVariable>>() {},
                Collections.singletonMap("foo", MetasyntacticVariable.FOO)))) instanceof Collection<?>);
    }

    @Test(expected = OperationException.class)
    public void testUnknownSourceElementType() {
        context.eval(Convert.to(new TypeLiteral<Collection<MetasyntacticVariable>>() {},
            Positions.readOnly(Collections.singletonMap("foo", MetasyntacticVariable.FOO))));
    }

    @Test(expected = OperationException.class)
    public void testIncompatibleElementType() {
        context.eval(Convert.to(
            new TypeLiteral<Iterable<String>>() {},
            Positions.readOnly(new TypeLiteral<Map<String, MetasyntacticVariable>>() {},
                Collections.singletonMap("foo", MetasyntacticVariable.FOO))));
    }
}
