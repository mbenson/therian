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
package therian.operator.immutablecheck;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.junit.Test;

import therian.TherianModule;
import therian.operation.ImmutableCheck;
import therian.operator.OperatorTest;
import therian.position.Position;
import therian.testfixture.MetasyntacticVariable;
import therian.util.Positions;

public class DefaultImmutableCheckerTest extends OperatorTest {
    @Override
    protected TherianModule[] modules() {
        return new TherianModule[] { TherianModule.create().withOperators(new DefaultImmutableChecker()) };
    }

    @Test
    public void testBasic() {
        assertTrue(therianContext.eval(ImmutableCheck.of(Positions.readOnly("foo"))).booleanValue());
        assertTrue(therianContext.eval(ImmutableCheck.of(Positions.readOnly(MetasyntacticVariable.FOO))).booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(MethodUtils.getAccessibleMethod(DefaultImmutableCheckerTest.class,
                "testBasic").getAnnotation(Test.class)))).booleanValue());
        assertTrue(therianContext.eval(ImmutableCheck.of(Positions.readOnly(Arrays.asList("foo", "bar", "baz")))));
    }

    @Test
    public void testNull() {
        assertTrue(therianContext.eval(ImmutableCheck.of(Positions.readOnly(Object.class, (Object) null))).booleanValue());
    }

    @Test
    public void testObject() {
        assertTrue(therianContext.eval(ImmutableCheck.of(Positions.readOnly(new Object()))).booleanValue());
    }

    @Test
    public void testPrimitive() {
        class RawPosition implements Position.Readable<Object> {
            final Type type;
            final Object value;

            RawPosition(Type type, Object value) {
                super();
                this.type = type;
                this.value = value;
            }

            public Type getType() {
                return type;
            }

            public Object getValue() {
                return value;
            }
        }
        ;
        assertTrue(therianContext.eval(ImmutableCheck.of(new RawPosition(int.class, 666))).booleanValue());
        assertTrue(therianContext.eval(ImmutableCheck.of(new RawPosition(long.class, 666L))).booleanValue());
        assertTrue(therianContext.eval(ImmutableCheck.of(new RawPosition(byte.class, (byte) 0))).booleanValue());
        assertTrue(therianContext.eval(ImmutableCheck.of(new RawPosition(short.class, (short) 0))).booleanValue());
        assertTrue(therianContext.eval(ImmutableCheck.of(new RawPosition(char.class, (char) 0))).booleanValue());
        assertTrue(therianContext.eval(ImmutableCheck.of(new RawPosition(float.class, 0.0f))).booleanValue());
        assertTrue(therianContext.eval(ImmutableCheck.of(new RawPosition(double.class, 0.0))).booleanValue());
        assertTrue(therianContext.eval(ImmutableCheck.of(new RawPosition(boolean.class, true))).booleanValue());
    }

    @Test
    public void testWrapper() {
        assertTrue(therianContext.eval(ImmutableCheck.of(Positions.readOnly(Integer.valueOf(666)))).booleanValue());
        assertTrue(therianContext.eval(ImmutableCheck.of(Positions.readOnly(Long.valueOf(666L)))).booleanValue());
        assertTrue(therianContext.eval(ImmutableCheck.of(Positions.readOnly(Byte.valueOf((byte) 0)))).booleanValue());
        assertTrue(therianContext.eval(ImmutableCheck.of(Positions.readOnly(Short.valueOf((short) 0)))).booleanValue());
        assertTrue(therianContext.eval(ImmutableCheck.of(Positions.readOnly(Character.valueOf((char) 0))))
            .booleanValue());
        assertTrue(therianContext.eval(ImmutableCheck.of(Positions.readOnly(Boolean.TRUE))).booleanValue());
        assertTrue(therianContext.eval(ImmutableCheck.of(Positions.readOnly(Float.valueOf(0.0f)))).booleanValue());
        assertTrue(therianContext.eval(ImmutableCheck.of(Positions.readOnly(Double.valueOf(0.0)))).booleanValue());
    }

    @Test
    public void testJavaUtilCollectionsFactoryTypes() {
        assertTrue(therianContext.eval(ImmutableCheck.of(Positions.readOnly(Collections.emptyList()))).booleanValue());
        assertTrue(therianContext.eval(ImmutableCheck.of(Positions.readOnly(Collections.emptySet()))).booleanValue());
        assertTrue(therianContext.eval(ImmutableCheck.of(Positions.readOnly(Collections.emptySet().iterator())))
            .booleanValue());
        assertTrue(therianContext.eval(ImmutableCheck.of(Positions.readOnly(Collections.emptyMap()))).booleanValue());
        assertTrue(therianContext.eval(ImmutableCheck.of(Positions.readOnly(Collections.emptyMap().keySet())))
            .booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(Collections.emptyMap().keySet().iterator()))).booleanValue());
        assertTrue(therianContext.eval(ImmutableCheck.of(Positions.readOnly(Collections.emptyMap().values())))
            .booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(Collections.emptyMap().values().iterator()))).booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(Collections.unmodifiableList(Collections.emptyList()))))
            .booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(Collections.unmodifiableList(Collections.emptyList()).iterator())))
            .booleanValue());
        assertTrue(therianContext
            .eval(
                ImmutableCheck.of(Positions.readOnly(Collections.unmodifiableList(Collections.emptyList())
                    .listIterator()))).booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(Collections.unmodifiableSet(Collections.emptySet())))).booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(Collections.unmodifiableSet(Collections.emptySet()).iterator())))
            .booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(Collections.unmodifiableMap(Collections.emptyMap())))).booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(Collections.unmodifiableMap(Collections.emptyMap()).keySet())))
            .booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(Collections.unmodifiableMap(Collections.emptyMap()).values())))
            .booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(Collections.unmodifiableMap(Collections.emptyMap()).keySet()
                .iterator()))).booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(Collections.unmodifiableMap(Collections.emptyMap()).values()
                .iterator()))).booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(Collections.unmodifiableSortedSet(new TreeSet<String>()))))
            .booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(Collections.unmodifiableSortedSet(new TreeSet<String>()).iterator())))
            .booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(Collections.unmodifiableSortedSet(new TreeSet<String>())
                .headSet("foo")))).booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(Collections.unmodifiableSortedSet(new TreeSet<String>())
                .tailSet("foo")))).booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(Collections.unmodifiableSortedSet(new TreeSet<String>()).subSet("foo",
                "foo")))).booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(Collections.unmodifiableSortedSet(new TreeSet<String>())
                .headSet("foo").iterator()))).booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(Collections.unmodifiableSortedSet(new TreeSet<String>())
                .tailSet("foo").iterator()))).booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(Collections.unmodifiableSortedSet(new TreeSet<String>())
                .subSet("foo", "foo").iterator()))).booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(Collections.unmodifiableSortedMap(new TreeMap<String, Object>()))))
            .booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(Collections.unmodifiableSortedMap(new TreeMap<String, Object>())
                .keySet()))).booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(Collections.unmodifiableSortedMap(new TreeMap<String, Object>())
                .values()))).booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(Collections.unmodifiableSortedMap(new TreeMap<String, Object>())
                .keySet().iterator()))).booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(Collections.unmodifiableSortedMap(new TreeMap<String, Object>())
                .values().iterator()))).booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(Collections.unmodifiableSortedMap(new TreeMap<String, Object>())
                .headMap("foo")))).booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(Collections.unmodifiableSortedMap(new TreeMap<String, Object>())
                .headMap("foo").keySet()))).booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(Collections.unmodifiableSortedMap(new TreeMap<String, Object>())
                .headMap("foo").values()))).booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(Collections.unmodifiableSortedMap(new TreeMap<String, Object>())
                .headMap("foo").keySet().iterator()))).booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(Collections.unmodifiableSortedMap(new TreeMap<String, Object>())
                .headMap("foo").values().iterator()))).booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(Collections.unmodifiableSortedMap(new TreeMap<String, Object>())
                .tailMap("foo")))).booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(Collections.unmodifiableSortedMap(new TreeMap<String, Object>())
                .tailMap("foo").keySet()))).booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(Collections.unmodifiableSortedMap(new TreeMap<String, Object>())
                .tailMap("foo").values()))).booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(Collections.unmodifiableSortedMap(new TreeMap<String, Object>())
                .tailMap("foo").keySet().iterator()))).booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(Collections.unmodifiableSortedMap(new TreeMap<String, Object>())
                .tailMap("foo").values().iterator()))).booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(Collections.unmodifiableSortedMap(new TreeMap<String, Object>())
                .subMap("foo", "foo")))).booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(Collections.unmodifiableSortedMap(new TreeMap<String, Object>())
                .subMap("foo", "foo").keySet()))).booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(Collections.unmodifiableSortedMap(new TreeMap<String, Object>())
                .subMap("foo", "foo").values()))).booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(Collections.unmodifiableSortedMap(new TreeMap<String, Object>())
                .subMap("foo", "foo").keySet().iterator()))).booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Positions.readOnly(Collections.unmodifiableSortedMap(new TreeMap<String, Object>())
                .subMap("foo", "foo").values().iterator()))).booleanValue());
    }

}
