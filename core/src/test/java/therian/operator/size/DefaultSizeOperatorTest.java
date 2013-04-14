package therian.operator.size;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import therian.TherianModule;
import therian.operation.Size;
import therian.operator.OperatorTest;
import therian.position.Ref;
import therian.testfixture.MetasyntacticVariable;

public class DefaultSizeOperatorTest extends OperatorTest {

    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new DefaultSizeOperator());
    }

    @Test
    public void test() {
        assertEquals(0, therianContext.eval(Size.of(new Ref<Object>(null) {})).intValue());
        assertEquals(0, therianContext.eval(Size.of(Ref.to(new String[0]))).intValue());
        assertEquals(MetasyntacticVariable.values().length,
            therianContext.eval(Size.of(Ref.to(MetasyntacticVariable.values()))).intValue());
        assertEquals(5, therianContext.eval(Size.of(Ref.to(new int[] { 0, 1, 2, 3, 4 }))).intValue());
        assertEquals(1, therianContext.eval(Size.of(Ref.to(new Object()))).intValue());
    }

}
