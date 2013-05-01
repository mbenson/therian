package therian.operator.getelementtype;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.junit.Test;

import therian.TherianModule;
import therian.TypeLiteral;
import therian.Typed;
import therian.operation.GetElementType;
import therian.operator.OperatorTest;
import therian.util.Types;

public class GetIterableElementTypeTest extends OperatorTest {

    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new GetIterableElementType());
    }

    @Test
    public void test() {
        assertEquals(String.class, therianContext.eval(GetElementType.of(new TypeLiteral<Iterable<String>>() {})));
        assertEquals(String.class, therianContext.eval(GetElementType.of(new TypeLiteral<List<String>>() {})));
        assertEquals(String.class, therianContext.eval(GetElementType.of(new TypeLiteral<Collection<String>>() {})));
        assertEquals(String.class, therianContext.eval(GetElementType.of(new TypeLiteral<Set<String>>() {})));
        assertEquals(String.class, therianContext.eval(GetElementType.of(new TypeLiteral<SortedSet<String>>() {})));
        assertTrue(Types.equals(new TypeLiteral<String[]>() {}.value,
            therianContext.eval(GetElementType.of(new TypeLiteral<List<String[]>>() {}))));
        assertTrue(Types.equals(new TypeLiteral<Typed<?>>() {}.value,
            therianContext.eval(GetElementType.of(new TypeLiteral<Iterable<Typed<?>>>() {}))));
    }

}
