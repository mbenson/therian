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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import therian.OperationException;
import therian.TherianModule;
import therian.TypeLiteral;
import therian.operation.Convert;
import therian.operator.OperatorTest;
import therian.testfixture.MetasyntacticVariable;
import therian.util.Positions;

public class IterableToIteratorTest extends OperatorTest {

    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new IterableToIterator());
    }

    @Test
    public void test() {
        assertTrue(therianContext.eval(Convert.to(Iterator.class,
            Positions.readOnly(Arrays.asList("foo", "bar", "baz")))) instanceof Iterator<?>);
        assertTrue(therianContext.eval(Convert.to(new TypeLiteral<Iterator<String>>() {},
            Positions.readOnly(new TypeLiteral<List<String>>() {}, Arrays.asList("foo", "bar", "baz")))) instanceof Iterator<?>);
    }

    @Test(expected = OperationException.class)
    public void testUnknownSourceElementType() {
        therianContext.eval(Convert.to(new TypeLiteral<Iterator<MetasyntacticVariable>>() {},
            Positions.readOnly(Arrays.asList("foo", "bar", "baz"))));
    }

    @Test(expected = OperationException.class)
    public void testIncompatibleElementType() {
        therianContext.eval(Convert.to(new TypeLiteral<Iterator<MetasyntacticVariable>>() {},
            Positions.readOnly(new TypeLiteral<List<String>>() {}, Arrays.asList("foo", "bar", "baz"))));
    }
}
