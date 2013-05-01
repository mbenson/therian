package therian.operator.getelementtype;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import org.junit.Test;

import therian.TherianModule;
import therian.TypeLiteral;
import therian.operation.GetElementType;
import therian.operator.OperatorTest;
import therian.util.Types;

public class GetMapElementTypeTest extends OperatorTest {

    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new GetMapElementType());
    }

    @Test
    public void test() {
        assertEquals(String.class, therianContext.eval(GetElementType.of(new TypeLiteral<Map<String, String>>() {})));
        assertEquals(Object.class, therianContext.eval(GetElementType.of(new TypeLiteral<Map<String, Object>>() {})));
        assertTrue(Types.equals(new TypeLiteral<List<String>>() {}.value,
            therianContext.eval(GetElementType.of(new TypeLiteral<Map<Integer, List<String>>>() {}))));
    }

}
