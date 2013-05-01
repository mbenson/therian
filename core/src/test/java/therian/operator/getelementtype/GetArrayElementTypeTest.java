package therian.operator.getelementtype;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import therian.TherianModule;
import therian.TypeLiteral;
import therian.operation.GetElementType;
import therian.operator.OperatorTest;
import therian.operator.getelementtype.GetArrayElementType;
import therian.util.Types;

public class GetArrayElementTypeTest extends OperatorTest {

    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new GetArrayElementType());
    }

    @Test
    public void test() {
        assertEquals(int.class, therianContext.eval(GetElementType.of(new TypeLiteral<int[]>() {})));
        assertEquals(String.class, therianContext.eval(GetElementType.of(new TypeLiteral<String[]>() {})));
        assertTrue(Types.equals(new TypeLiteral<List<String>>() {}.value,
            therianContext.eval(GetElementType.of(new TypeLiteral<List<String>[]>() {}))));
        assertTrue(Types.equals(new TypeLiteral<Object[]>() {}.value,
            therianContext.eval(GetElementType.of(new TypeLiteral<Object[][]>() {}))));
    }

}
