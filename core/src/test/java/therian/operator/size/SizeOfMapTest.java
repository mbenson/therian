package therian.operator.size;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.apache.commons.lang3.EnumUtils;
import org.junit.Test;

import therian.TherianModule;
import therian.operation.Size;
import therian.operator.OperatorTest;
import therian.position.Ref;
import therian.testfixture.MetasyntacticVariable;

public class SizeOfMapTest extends OperatorTest {

    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new SizeOfMap());
    }

    @Test
    public void test() {
        assertEquals(0, therianContext.eval(Size.of(Ref.to(Collections.emptyMap()))).intValue());
        assertEquals(1, therianContext
            .eval(Size.of(Ref.to(Collections.singletonMap("foo", MetasyntacticVariable.FOO)))).intValue());
        assertEquals(MetasyntacticVariable.values().length,
            therianContext.eval(Size.of(Ref.to(EnumUtils.getEnumMap(MetasyntacticVariable.class)))).intValue());
    }

}
