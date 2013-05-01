package therian.operator.getelementtype;

import static org.junit.Assert.*;

import java.util.Enumeration;
import java.util.List;

import org.junit.Test;

import therian.TherianModule;
import therian.TypeLiteral;
import therian.operation.GetElementType;
import therian.operator.OperatorTest;
import therian.util.Types;

public class GetEnumerationElementTypeTest extends OperatorTest {

    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new GetEnumerationElementType());
    }

    @Test
    public void test() {
        assertEquals(String.class, therianContext.eval(GetElementType.of(new TypeLiteral<Enumeration<String>>() {})));
        assertTrue(Types.equals(new TypeLiteral<List<String>>() {}.value,
            therianContext.eval(GetElementType.of(new TypeLiteral<Enumeration<List<String>>>() {}))));
        assertTrue(Types.equals(new TypeLiteral<String[]>() {}.value,
            therianContext.eval(GetElementType.of(new TypeLiteral<Enumeration<String[]>>() {}))));
    }

}
