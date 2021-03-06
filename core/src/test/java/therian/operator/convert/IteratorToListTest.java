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
import static therian.util.Positions.readWrite;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.junit.Test;

import therian.TherianModule;
import therian.operation.Convert;
import therian.operator.OperatorTest;

/**
 *
 */
public class IteratorToListTest extends OperatorTest {

    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new IteratorToList());
    }

    @Test
    public void test() {
        final List<String> l = Arrays.asList("foo", "bar", "baz");

        final TypeLiteral<Iterator<String>> type = new TypeLiteral<Iterator<String>>() {};
        assertEquals(l, therianContext.eval(Convert.to(Iterable.class, readWrite(type, l.iterator()))));
        assertEquals(l, therianContext.eval(Convert.to(Collection.class, readWrite(type, l.iterator()))));
        assertEquals(l, therianContext.eval(Convert.to(List.class, readWrite(type, l.iterator()))));

        assertEquals(l,
            therianContext.eval(Convert.to(new TypeLiteral<Iterable<String>>() {}, readWrite(type, l.iterator()))));
        assertEquals(l,
            therianContext.eval(Convert.to(new TypeLiteral<Collection<String>>() {}, readWrite(type, l.iterator()))));
        assertEquals(l,
            therianContext.eval(Convert.to(new TypeLiteral<List<String>>() {}, readWrite(type, l.iterator()))));
    }

}
