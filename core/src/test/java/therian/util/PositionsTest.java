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
package therian.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.junit.Test;

import therian.position.Position;

/**
 *
 */
public class PositionsTest {

    @Test
    public void testConstantOfStronglyTypedObject() {
        final Position.Readable<String> pos = Positions.readOnly("foo");
        assertEquals(String.class, pos.getType());
        assertEquals("foo", pos.getValue());
    }

    @Test(expected = NullPointerException.class)
    public void testNullValueFailure() {
        Positions.readOnly(null);
    }

    @Test
    public void testNullValueWithType() {
        final Position.Readable<String> pos = Positions.readOnly(String.class, null);
        assertEquals(String.class, pos.getType());
        assertNull(pos.getValue());
    }

    @Test
    public void testNarrowestParameterizedType() {
        final TypeLiteral<List<String>> listOfStringType = new TypeLiteral<List<String>>() {};
        final Position.ReadWrite<List<String>> rw = Positions.readWrite(listOfStringType);
        assertEquals(listOfStringType.value, Positions.narrowestParameterizedType(rw));

        rw.setValue(Arrays.asList("foo", "bar", "baz"));
        assertEquals(TypeUtils.parameterize(rw.getValue().getClass(), String.class),
            Positions.narrowestParameterizedType(rw));

        rw.setValue(new ArrayList<String>(rw.getValue()));
        assertEquals(TypeUtils.parameterize(ArrayList.class, String.class), Positions.narrowestParameterizedType(rw));

        rw.setValue(rw.getValue().subList(0, 1));
        assertEquals(TypeUtils.parameterize(AbstractList.class, String.class), Positions.narrowestParameterizedType(rw));
    }

}
