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
import org.junit.Test;

import therian.position.Ref;

public class ElementTest {

    @Test
    public void testGetIterableElementType() {
        final List<String> l = new ArrayList<String>();
        assertEquals(String.class, Element.atIndex(0).of(new Ref<List<String>>(l) {}).getType());
        assertEquals(Object.class, Element.atIndex(0).of(Ref.to(l)).getType());
    }

    @Test
    public void testGetIterableElementValue() {
        final List<String> l = new ArrayList<String>();
        l.add("foo");
        assertEquals("foo", Element.atIndex(0).of(Ref.to(l)).getValue());
    }

    @Test
    public void testGetOutOfBoundsIterableElementValue() {
        assertNull(Element.atIndex(0).of(Ref.to(Collections.emptyList())).getValue());
    }

    @Test
    public void testSetIterableElementValue() {
        final List<String> l = new ArrayList<String>();
        l.add("foo");
        Element.atIndex(0).of(Ref.to(l)).setValue("bar");
        assertEquals("bar", l.get(0));
    }

    @Test
    public void testGetArrayElementType() {
        assertEquals(boolean.class, Element.atArrayIndex(0).of(Ref.to(ArrayUtils.EMPTY_BOOLEAN_ARRAY)).getType());
        assertEquals(Boolean.class, Element.atArrayIndex(0).of(Ref.to(ArrayUtils.toArray(Boolean.TRUE))).getType());
        assertEquals(Object.class, Element.atArrayIndex(0).of(Ref.to(ArrayUtils.EMPTY_OBJECT_ARRAY)).getType());
        assertEquals(String.class, Element.atArrayIndex(0).of(Ref.to(ArrayUtils.EMPTY_STRING_ARRAY)).getType());
    }

    @Test
    public void testGetArrayElementValue() {
        assertEquals("foo", Element.atArrayIndex(0).of(Ref.to(ArrayUtils.toArray("foo"))).getValue());
    }

    @Test
    public void testGetOutOfBoundsArrayElementValue() {
        assertNull(Element.atArrayIndex(0).of(Ref.to(ArrayUtils.EMPTY_STRING_ARRAY)).getValue());
    }

    @Test
    public void testSetArrayElementValue() {
        final String[] array = ArrayUtils.toArray("foo");
        Element.atArrayIndex(0).of(Ref.to(array)).setValue("bar");
        assertEquals("bar", array[0]);
    }

    @Test
    public void testToString() {
        final Ref<ArrayList<String>> listRef = Ref.to(new ArrayList<String>());
        assertEquals(String.format("Relative Position: Element [0] of %s", listRef), Element.atIndex(0).of(listRef)
            .toString());

        final Ref<String[]> arrayRef = Ref.to(new String[0]);
        assertEquals(String.format("Relative Position: Array Element [0] of %s", arrayRef),
            Element.atArrayIndex(0).of(arrayRef).toString());
    }
}
