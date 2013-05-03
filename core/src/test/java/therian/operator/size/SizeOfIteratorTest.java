package therian.operator.size;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import therian.TherianModule;
import therian.operation.Size;
import therian.operator.OperatorTest;
import therian.testfixture.MetasyntacticVariable;
import therian.util.Positions;

public class SizeOfIteratorTest extends OperatorTest {

    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new SizeOfIterator());
    }

    @Test
    public void test() {
        assertEquals(0, therianContext.eval(Size.of(Positions.readOnly(Collections.emptyList().iterator()))).intValue());
        assertEquals(1, therianContext.eval(Size.of(Positions.readOnly(Collections.singleton("foo").iterator())))
            .intValue());
        assertEquals(MetasyntacticVariable.values().length,
            therianContext.eval(Size.of(Positions.readOnly(Arrays.asList(MetasyntacticVariable.values()).iterator())))
                .intValue());
    }

}
