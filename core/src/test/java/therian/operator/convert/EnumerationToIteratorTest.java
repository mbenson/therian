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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.junit.Test;

import therian.TherianModule;
import therian.TypeLiteral;
import therian.operation.Convert;
import therian.operator.OperatorTest;
import therian.util.Positions;

/**
 * Test {@link EnumerationToIterator}
 */
public class EnumerationToIteratorTest extends OperatorTest {

    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new EnumerationToIterator(), new EnumerationToList(),
            new IterableToIterator());
    }

    @Test
    public void test() {
        class Tokens implements Enumeration<String> {
            final StringTokenizer tok;

            Tokens(String s) {
                tok = new StringTokenizer(s);
            }

            @Override
            public boolean hasMoreElements() {
                return tok.hasMoreTokens();
            }

            @Override
            public String nextElement() {
                return tok.nextToken();
            }
        }

        final Enumeration<String> tokens = new Tokens("foo bar baz");

        final Iterator<String> iter =
            therianContext.eval(Convert.to(new TypeLiteral<Iterator<String>>() {}, Positions.readOnly(tokens)));
        for (String s : Arrays.asList("foo", "bar", "baz")) {
            assertTrue(iter.hasNext());
            assertEquals(s, iter.next());
        }
        assertFalse(iter.hasNext());
    }

    @Test
    public void testSupports() {
        assertFalse(therianContext.supports(Convert.to(new TypeLiteral<Iterator<String>>() {},
            Positions.readOnly(new StringTokenizer("foo")))));
    }
}
