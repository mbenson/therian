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
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;

import org.junit.Test;

import therian.TherianModule;
import therian.TypeLiteral;
import therian.operation.Convert;
import therian.operator.OperatorTest;
import therian.position.Ref;

/**
 *
 */
public class EnumerationToListTest extends OperatorTest {

    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new EnumerationToList());
    }

    @Test
    public void test() {
        final String s = "foo bar baz";

        final List<String> expected = Arrays.asList("foo", "bar", "baz");

        assertEquals(expected, therianContext.eval(Convert.to(new TypeLiteral<Iterable<String>>() {},
            new Ref<Enumeration<String>>(tokenize(s)) {})));

        assertEquals(expected, therianContext.eval(Convert.to(new TypeLiteral<List<String>>() {},
            new Ref<Enumeration<String>>(tokenize(s)) {})));

        assertEquals(expected, therianContext.eval(Convert.to(new TypeLiteral<List<Object>>() {},
            new Ref<Enumeration<String>>(tokenize(s)) {})));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Enumeration<String> tokenize(String s) {
        final Enumeration raw = new StringTokenizer(s);
        return raw;
    }
}
