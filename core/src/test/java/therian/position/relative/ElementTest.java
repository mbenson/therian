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
package therian.position.relative;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.TypeLiteral;
import org.junit.Test;

import therian.position.Position;
import therian.util.Positions;

public class ElementTest {

    @Test
    public void testGetIterableElementType() {
        final List<String> l = new ArrayList<String>();
        assertEquals(String.class, Element.atIndex(0).of(Positions.readOnly(new TypeLiteral<List<String>>() {}, l))
            .getType());
        assertEquals(Object.class, Element.atIndex(0).of(Positions.readOnly(l)).getType());
    }

    @Test
    public void testGetIterableElementValue() {
        final List<String> l = new ArrayList<String>();
        l.add("foo");
        assertEquals("foo", Element.atIndex(0).of(Positions.readOnly(l)).getValue());
    }

    @Test
    public void testGetOutOfBoundsIterableElementValue() {
        assertNull(Element.atIndex(0).of(Positions.readOnly(Collections.emptyList())).getValue());
    }

    @Test
    public void testSetIterableElementValue() {
        final List<String> l = new ArrayList<String>();
        l.add("foo");
        Element.atIndex(0).of(Positions.readOnly(l)).setValue("bar");
        assertEquals("bar", l.get(0));
    }

    @Test
    public void testGetArrayElementType() {
        assertEquals(boolean.class, Element.atArrayIndex(0).of(Positions.readOnly(ArrayUtils.EMPTY_BOOLEAN_ARRAY))
            .getType());
        assertEquals(Boolean.class, Element.atArrayIndex(0).of(Positions.readOnly(ArrayUtils.toArray(Boolean.TRUE)))
            .getType());
        assertEquals(Object.class, Element.atArrayIndex(0).of(Positions.readOnly(ArrayUtils.EMPTY_OBJECT_ARRAY))
            .getType());
        assertEquals(String.class, Element.atArrayIndex(0).of(Positions.readOnly(ArrayUtils.EMPTY_STRING_ARRAY))
            .getType());
    }

    @Test
    public void testGetArrayElementValue() {
        Position.Readable<String[]> array = Positions.readOnly(ArrayUtils.toArray("foo"));
        RelativePosition.ReadWrite<String[], String> foo = Element.<String> atArrayIndex(0).of(array);
        assertEquals("foo", foo.getValue());
    }

    @Test
    public void testGetOutOfBoundsArrayElementValue() {
        assertNull(Element.atArrayIndex(0).of(Positions.readOnly(ArrayUtils.EMPTY_STRING_ARRAY)).getValue());
    }

    @Test
    public void testSetArrayElementValue() {
        final String[] array = ArrayUtils.toArray("foo");
        Element.atArrayIndex(0).of(Positions.readOnly(array)).setValue("bar");
        assertEquals("bar", array[0]);
    }

    @Test
    public void testToString() {
        final Position.Readable<ArrayList<String>> listRef = Positions.readOnly(new ArrayList<String>());
        assertEquals(String.format("Relative Position: Element [0] of %s", listRef), Element.atIndex(0).of(listRef)
            .toString());

        final Position.Readable<String[]> arrayRef = Positions.readOnly(new String[0]);
        assertEquals(String.format("Relative Position: Array Element [0] of %s", arrayRef),
            Element.atArrayIndex(0).of(arrayRef).toString());
    }
}
