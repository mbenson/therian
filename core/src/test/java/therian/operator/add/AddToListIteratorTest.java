package therian.operator.add;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.junit.Test;

import therian.OperationException;
import therian.TherianModule;
import therian.operation.Add;
import therian.operator.OperatorTest;
import therian.util.Positions;

public class AddToListIteratorTest extends OperatorTest {

    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new AddToListIterator());
    }

    @Test
    public void testRaw() {
        final List<?> l = new ArrayList<Object>();
        assertTrue(therianContext.eval(
            Add.to(Positions.readWrite(ListIterator.class, l.listIterator()), Positions.readOnly("foo")))
            .booleanValue());
        assertEquals(1, l.size());
        assertEquals("foo", l.get(0));
    }

    @Test
    public void testTyped() {
        final List<String> l = new ArrayList<String>();
        assertTrue(therianContext.eval(
            Add.to(Positions.readWrite(new TypeLiteral<ListIterator<String>>() {}.value, l.listIterator()),
                Positions.readOnly("foo"))).booleanValue());
        assertEquals(1, l.size());
        assertEquals("foo", l.get(0));
    }

    @Test(expected = OperationException.class)
    public void testImmutable() {
        therianContext.eval(Add.to(
            Positions.readWrite(new TypeLiteral<ListIterator<String>>() {},
                Collections.unmodifiableList(Collections.<String> emptyList()).listIterator()),
            Positions.readOnly("foo")));
    }

    @Test(expected = OperationException.class)
    public void testWrongType() {
        therianContext.eval(Add.to(
            Positions.readWrite(new TypeLiteral<ListIterator<String>>() {}, new ArrayList<String>().listIterator()),
            Positions.readOnly(new Object())));
    }

    @Test
    public void testHasContent() {
        final List<String> l = new ArrayList<String>(Arrays.asList("foo", "bar", "baz"));
        final ListIterator<String> listIterator = l.listIterator();
        listIterator.next();
        listIterator.next();
        assertEquals(2, listIterator.nextIndex());
        assertTrue(therianContext.eval(
            Add.to(Positions.readWrite(new TypeLiteral<ListIterator<String>>() {}, listIterator),
                Positions.readOnly("blah"))).booleanValue());
        assertEquals(2, listIterator.nextIndex());
        assertEquals("baz", listIterator.next());
        assertEquals("blah", listIterator.next());
    }
}
