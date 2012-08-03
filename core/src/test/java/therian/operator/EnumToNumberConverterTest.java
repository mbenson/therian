package therian.operator;

import static org.junit.Assert.*;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import therian.TherianModule;
import therian.operation.Convert;
import therian.position.Ref;
import therian.testfixture.MetasyntacticVariable;

public class EnumToNumberConverterTest extends TransformerTest {

    @Override
    protected TherianModule[] modules() {
        return ArrayUtils.toArray(TherianModule.create().withOperators(new EnumToNumberConverter(),
            new ELCoercionConverter()));
    }

    @Test
    public void testConversion() {
        assertEquals(Double.valueOf(0.0),
            therianContext.eval(Convert.to(Double.class, Ref.to(MetasyntacticVariable.FOO))));
        assertEquals(1, therianContext.eval(Convert.to(Integer.class, Ref.to(MetasyntacticVariable.BAR))).intValue());
        assertEquals(2L, therianContext.eval(Convert.to(Long.class, Ref.to(MetasyntacticVariable.BAZ))).longValue());
    }

}
