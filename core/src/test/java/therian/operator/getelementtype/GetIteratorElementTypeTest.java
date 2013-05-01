package therian.operator.getelementtype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Test;

import therian.TherianModule;
import therian.TypeLiteral;
import therian.operation.GetElementType;
import therian.operator.OperatorTest;
import therian.util.Types;

public class GetIteratorElementTypeTest extends OperatorTest {

    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new GetIteratorElementType());
    }

    @Test
    public void test() {
        assertEquals(String.class, therianContext.eval(GetElementType.of(new TypeLiteral<Iterator<String>>() {})));
        assertTrue(Types.equals(new TypeLiteral<String[]>() {}.value,
            therianContext.eval(GetElementType.of(new TypeLiteral<Iterator<String[]>>() {}))));
        assertTrue(Types.equals(new TypeLiteral<Class<?>>() {}.value,
            therianContext.eval(GetElementType.of(new TypeLiteral<Iterator<Class<?>>>() {}))));
    }

}
