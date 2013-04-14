package therian.operator.add;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import therian.OperationException;
import therian.TherianModule;
import therian.TypeLiteral;
import therian.operation.Add;
import therian.operator.OperatorTest;
import therian.operator.immutablecheck.DefaultImmutableChecker;
import therian.position.Box;
import therian.position.Ref;

public class AddToCollectionTest extends OperatorTest {

    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new AddToCollection(), new DefaultImmutableChecker());
    }

    @Test
    public void testRawList() {
        final List<?> l = new ArrayList<Object>();
        assertTrue(therianContext.eval(Add.to(new Box<List<?>>(List.class, l), Ref.to("foo"))).booleanValue());
        assertEquals(1, l.size());
        assertEquals("foo", l.get(0));
    }

    @Test
    public void testTypedList() {
        final List<String> l = new ArrayList<String>();
        assertTrue(therianContext.eval(
            Add.to(new Box<List<String>>(new TypeLiteral<List<String>>() {}.value, l), Ref.to("foo"))).booleanValue());
        assertEquals(1, l.size());
        assertEquals("foo", l.get(0));
    }

    @Test(expected = OperationException.class)
    public void testImmutableList() {
        therianContext.eval(Add.to(new Box.Unchecked<List<String>>(new TypeLiteral<List<String>>() {}.value,
            Collections.<String> emptyList()), Ref.to("foo")));
    }

    @Test(expected = OperationException.class)
    public void testWrongTypeList() {
        therianContext.eval(Add.to(new Box<List<String>>(new TypeLiteral<List<String>>() {}.value,
            new ArrayList<String>()), Ref.to(new Object())));
    }

    @Test
    public void testRawSet() {
        final Set<?> s = new HashSet<Object>();
        assertTrue(therianContext.eval(Add.to(new Box<Set<?>>(Set.class, s), Ref.to("foo"))).booleanValue());
        assertEquals(1, s.size());
        assertEquals("foo", s.iterator().next());
    }

    @Test
    public void testTypedSet() {
        final Set<String> s = new HashSet<String>();
        assertTrue(therianContext.eval(
            Add.to(new Box<Set<String>>(new TypeLiteral<Set<String>>() {}.value, s), Ref.to("foo"))).booleanValue());
        assertEquals(1, s.size());
        assertEquals("foo", s.iterator().next());
    }

    @Test(expected = OperationException.class)
    public void testImmutableSet() {
        final Set<String> s = Collections.singleton("bar");
        assertTrue(therianContext.eval(
            Add.to(new Box<Set<String>>(new TypeLiteral<Set<String>>() {}.value, s), Ref.to("foo"))).booleanValue());
    }

    @Test(expected = OperationException.class)
    public void testWrongTypeSet() {
        therianContext
            .eval(Add.to(new Box<Set<String>>(new TypeLiteral<Set<String>>() {}.value, new HashSet<String>()),
                Ref.to(new Object())));
    }
}
