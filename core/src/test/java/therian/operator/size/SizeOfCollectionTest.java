package therian.operator.size;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import therian.TherianModule;
import therian.operation.Size;
import therian.operator.OperatorTest;
import therian.position.Ref;
import therian.testfixture.MetasyntacticVariable;

public class SizeOfCollectionTest extends OperatorTest {

    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new SizeOfCollection());
    }

    @Test
    public void test() {
        assertEquals(0, therianContext.eval(Size.of(Ref.to(Collections.emptyList()))).intValue());
        assertEquals(1, therianContext.eval(Size.of(Ref.to(Collections.singleton("foo")))).intValue());
        assertEquals(MetasyntacticVariable.values().length,
            therianContext.eval(Size.of(Ref.to(Arrays.asList(MetasyntacticVariable.values())))).intValue());
    }

}
