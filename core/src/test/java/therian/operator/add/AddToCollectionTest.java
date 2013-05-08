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
import therian.util.Positions;

public class AddToCollectionTest extends OperatorTest {

    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new AddToCollection(), new DefaultImmutableChecker());
    }

    @Test
    public void testRawList() {
        final List<?> l = new ArrayList<Object>();
        assertTrue(therianContext.eval(Add.to(Positions.readWrite(List.class, l), Positions.readOnly("foo")))
            .booleanValue());
        assertEquals(1, l.size());
        assertEquals("foo", l.get(0));
    }

    @Test
    public void testTypedList() {
        final List<String> l = new ArrayList<String>();
        assertTrue(therianContext.eval(
            Add.to(Positions.readWrite(new TypeLiteral<List<String>>() {}, l), Positions.readOnly("foo")))
            .booleanValue());
        assertEquals(1, l.size());
        assertEquals("foo", l.get(0));
    }

    @SuppressWarnings("rawtypes")
    @Test(expected = OperationException.class)
    public void testImmutableList() {
        therianContext.eval(Add.to(Positions.readWrite(new TypeLiteral<List>() {}, Collections.<String> emptyList()),
            Positions.readOnly("foo")));
    }

    @Test(expected = OperationException.class)
    public void testWrongTypeList() {
        therianContext.eval(Add.to(Positions.readWrite(new TypeLiteral<List<String>>() {}, new ArrayList<String>()),
            Positions.readOnly(new Object())));
    }

    @Test
    public void testRawSet() {
        final Set<?> s = new HashSet<Object>();
        assertTrue(therianContext.eval(Add.to(Positions.readWrite(Set.class, s), Positions.readOnly("foo")))
            .booleanValue());
        assertEquals(1, s.size());
        assertEquals("foo", s.iterator().next());
    }

    @Test
    public void testTypedSet() {
        final Set<String> s = new HashSet<String>();
        assertTrue(therianContext.eval(
            Add.to(Positions.readWrite(new TypeLiteral<Set<String>>() {}, s), Positions.readOnly("foo")))
            .booleanValue());
        assertEquals(1, s.size());
        assertEquals("foo", s.iterator().next());
    }

    @Test(expected = OperationException.class)
    public void testImmutableSet() {
        final Set<String> s = Collections.singleton("bar");
        assertTrue(therianContext.eval(
            Add.to(Positions.readWrite(new TypeLiteral<Set<String>>() {}, s), Positions.readOnly("foo")))
            .booleanValue());
    }

    @Test(expected = OperationException.class)
    public void testWrongTypeSet() {
        therianContext.eval(Add.to(Positions.readWrite(new TypeLiteral<Set<String>>() {}, new HashSet<String>()),
            Positions.readOnly(new Object())));
    }
}
