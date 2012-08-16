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
import java.util.Collections;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.junit.Test;

import therian.TherianModule;
import therian.operation.ImmutableCheck;
import therian.operator.TransformerTest;
import therian.position.Position;
import therian.position.Ref;
import therian.testfixture.MetasyntacticVariable;

public class DefaultImmutableCheckerTest extends TransformerTest {
    @Override
    protected TherianModule[] modules() {
        return new TherianModule[] { TherianModule.create().withOperators(new DefaultImmutableChecker()) };
    }

    @Test
    public void testBasic() {
        assertTrue(therianContext.eval(ImmutableCheck.of(Ref.to("foo"))).booleanValue());
        assertTrue(therianContext.eval(ImmutableCheck.of(Ref.to(MetasyntacticVariable.FOO))).booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Ref.to(MethodUtils.getAccessibleMethod(DefaultImmutableCheckerTest.class, "testBasic")
                .getAnnotation(Test.class)))).booleanValue());
    }

    @Test
    public void testNull() {
        assertTrue(therianContext.eval(ImmutableCheck.of(new Ref<Object>(null) {})).booleanValue());
    }

    @Test
    public void testObject() {
        assertTrue(therianContext.eval(ImmutableCheck.of(Ref.to(new Object()))).booleanValue());
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
        assertTrue(therianContext.eval(ImmutableCheck.of(Ref.to(Integer.valueOf(666)))).booleanValue());
        assertTrue(therianContext.eval(ImmutableCheck.of(Ref.to(Long.valueOf(666L)))).booleanValue());
        assertTrue(therianContext.eval(ImmutableCheck.of(Ref.to(Byte.valueOf((byte) 0)))).booleanValue());
        assertTrue(therianContext.eval(ImmutableCheck.of(Ref.to(Short.valueOf((short) 0)))).booleanValue());
        assertTrue(therianContext.eval(ImmutableCheck.of(Ref.to(Character.valueOf((char) 0)))).booleanValue());
        assertTrue(therianContext.eval(ImmutableCheck.of(Ref.to(Boolean.TRUE))).booleanValue());
        assertTrue(therianContext.eval(ImmutableCheck.of(Ref.to(Float.valueOf(0.0f)))).booleanValue());
        assertTrue(therianContext.eval(ImmutableCheck.of(Ref.to(Double.valueOf(0.0)))).booleanValue());
    }

    @Test
    public void testJavaUtilCollectionsFactoryTypes() {
        assertTrue(therianContext.eval(ImmutableCheck.of(Ref.to(Collections.emptyList()))).booleanValue());
        assertTrue(therianContext.eval(ImmutableCheck.of(Ref.to(Collections.emptySet()))).booleanValue());
        assertTrue(therianContext.eval(ImmutableCheck.of(Ref.to(Collections.emptyMap()))).booleanValue());
        assertTrue(therianContext
            .eval(ImmutableCheck.of(Ref.to(Collections.unmodifiableList(Collections.emptyList())))).booleanValue());
        assertTrue(therianContext.eval(ImmutableCheck.of(Ref.to(Collections.unmodifiableSet(Collections.emptySet()))))
            .booleanValue());
        assertTrue(therianContext.eval(ImmutableCheck.of(Ref.to(Collections.unmodifiableMap(Collections.emptyMap()))))
            .booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Ref.to(Collections.unmodifiableSortedSet(new TreeSet<String>())))).booleanValue());
        assertTrue(therianContext.eval(
            ImmutableCheck.of(Ref.to(Collections.unmodifiableSortedMap(new TreeMap<String, Object>())))).booleanValue());
    }

}
