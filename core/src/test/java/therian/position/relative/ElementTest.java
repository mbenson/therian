package therian.position.relative;

import static org.junit.Assert.*;

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
}
