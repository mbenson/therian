package therian.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import therian.Operation;
import therian.TypeLiteral;
import therian.operation.Convert;
import therian.operation.Copy;
import therian.operation.Size;
import therian.operation.Transform;
import therian.operator.size.SizeOfCollection;
import therian.operator.size.SizeOfIterable;
import therian.operator.size.SizeOfIterator;
import therian.testfixture.MetasyntacticVariable;

public class TypesTest {

    @Test
    public void testGetSimpleType() {
        assertEquals(new TypeLiteral<ArrayList<String>>() {}.value, Types.resolveAt(
            Size.of(Positions.<ArrayList<String>> readWrite(new TypeLiteral<ArrayList<String>>() {})),
            Size.class.getTypeParameters()[0]));
    }

    @Test
    public void testGetInheritedType() {
        final Copy<Integer, String> copy =
            Copy.to(Positions.<String> readWrite(String.class), Positions.readOnly(Integer.valueOf(666)));
        assertEquals(Integer.class, Types.resolveAt(copy, Transform.class.getTypeParameters()[0]));
        assertEquals(String.class, Types.resolveAt(copy, Transform.class.getTypeParameters()[1]));
        assertEquals(Integer.class, Types.resolveAt(copy, Copy.class.getTypeParameters()[0]));
        assertEquals(String.class, Types.resolveAt(copy, Copy.class.getTypeParameters()[1]));
        final Convert<Integer, String> convert = Convert.to(String.class, Positions.readOnly(Integer.valueOf(666)));
        assertEquals(Integer.class, Types.resolveAt(convert, Transform.class.getTypeParameters()[0]));
        assertEquals(String.class, Types.resolveAt(convert, Transform.class.getTypeParameters()[1]));
        assertEquals(Integer.class, Types.resolveAt(convert, Convert.class.getTypeParameters()[0]));
        assertEquals(String.class, Types.resolveAt(convert, Convert.class.getTypeParameters()[1]));
    }

    public void testNonTyped() {
        Assert.assertNull(Types.resolveAt(
            Copy.to(Positions.<String> readWrite(String.class), Positions.readOnly(Integer.valueOf(666))),
            Operation.class.getTypeParameters()[0]));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadTypeVariable() throws Exception {
        Types.resolveAt(Convert.to(String.class, Positions.readOnly(Integer.valueOf(666))), getClass()
            .getDeclaredMethod("foo").getTypeParameters()[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotAssignableTypeVariable() {
        Types.resolveAt(Convert.to(String.class, Positions.readOnly(Integer.valueOf(666))),
            Copy.class.getTypeParameters()[0]);
    }

    @Test
    public void testMatchesOperator() {
        final Size<List<MetasyntacticVariable>> size =
            Size.of(Positions.<List<MetasyntacticVariable>> readOnly(Arrays.asList(MetasyntacticVariable.values())));
        assertTrue(size.matches(new SizeOfCollection()));
        assertTrue(size.matches(new SizeOfIterable()));
        assertFalse(size.matches(new SizeOfIterator()));
    }

    @SuppressWarnings("unused")
    private <T> void foo() {

    }
}
