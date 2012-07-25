package net.morph.position;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class ConstantTest {

    @Test
    public void testConstantOfStronglyTypedObject() {
        final Position.Readable<String> pos = Constant.of("foo");
        assertEquals(String.class, pos.getType());
        assertEquals("foo", pos.getValue());
    }

    @Test(expected = NullPointerException.class)
    public void testConstantOfNullFailure() {
        Constant.of(null);
    }

    @Test
    public void testAnonymousConstantSubclassWithNullValue() {
        final Position.Readable<String> pos = new Constant<String>(null) {
        };
        assertEquals(String.class, pos.getType());
        assertNull(pos.getValue());
    }
}
