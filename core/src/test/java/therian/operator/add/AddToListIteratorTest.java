package therian.operator.add;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.junit.Test;

import therian.OperationException;
import therian.TherianModule;
import therian.TypeLiteral;
import therian.operation.Add;
import therian.operator.OperatorTest;
import therian.operator.immutablecheck.DefaultImmutableChecker;
import therian.position.Box;
import therian.position.Ref;

public class AddToListIteratorTest extends OperatorTest {

    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new AddToListIterator(), new DefaultImmutableChecker());
    }

    @Test
    public void testRaw() {
        final List<?> l = new ArrayList<Object>();
        assertTrue(therianContext.eval(
            Add.to(new Box<ListIterator<?>>(ListIterator.class, l.listIterator()), Ref.to("foo"))).booleanValue());
        assertEquals(1, l.size());
        assertEquals("foo", l.get(0));
    }

    @Test
    public void testTyped() {
        final List<String> l = new ArrayList<String>();
        assertTrue(therianContext.eval(
            Add.to(new Box<ListIterator<String>>(new TypeLiteral<ListIterator<String>>() {}.value, l.listIterator()),
                Ref.to("foo"))).booleanValue());
        assertEquals(1, l.size());
        assertEquals("foo", l.get(0));
    }

    @Test(expected = OperationException.class)
    public void testImmutable() {
        therianContext.eval(Add.to(new Box<ListIterator<String>>(new TypeLiteral<ListIterator<String>>() {}.value,
            Collections.unmodifiableList(Collections.<String> emptyList()).listIterator()), Ref.to("foo")));
    }

    @Test(expected = OperationException.class)
    public void testWrongType() {
        therianContext.eval(Add.to(new Box<ListIterator<String>>(new TypeLiteral<ListIterator<String>>() {}.value,
            new ArrayList<String>().listIterator()), Ref.to(new Object())));
    }

    @Test
    public void testHasContent() {
        final List<String> l = new ArrayList<String>(Arrays.asList("foo", "bar", "baz"));
        final ListIterator<String> listIterator = l.listIterator();
        listIterator.next();
        listIterator.next();
        assertEquals(2, listIterator.nextIndex());
        assertTrue(therianContext.eval(
            Add.to(new Box<ListIterator<String>>(new TypeLiteral<ListIterator<String>>() {}.value, listIterator),
                Ref.to("blah"))).booleanValue());
        assertEquals(2, listIterator.nextIndex());
        assertEquals("baz", listIterator.next());
        assertEquals("blah", listIterator.next());
    }
}
