package therian.operator.add;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import therian.OperationException;
import therian.TherianModule;
import therian.operation.Add;
import therian.operator.OperatorTest;
import therian.position.Position;
import therian.testfixture.MetasyntacticVariable;
import therian.util.Positions;

public class AddToArrayTest extends OperatorTest {

    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new AddToArray());
    }

    @Test
    public void testAddToEmptyArray() {
        final Position.ReadWrite<String[]> pos = Positions.readWrite(String[].class, new String[0]);
        assertTrue(therianContext.eval(Add.to(pos, Positions.readOnly("foo"))).booleanValue());
        assertEquals(1, pos.getValue().length);
        assertEquals("foo", pos.getValue()[0]);
    }

    @Test
    public void testAddToPopulatedArray() {
        final Position.ReadWrite<MetasyntacticVariable[]> pos =
            Positions.readWrite(MetasyntacticVariable[].class, new MetasyntacticVariable[] { MetasyntacticVariable.FOO,
                MetasyntacticVariable.BAR });
        assertTrue(therianContext.eval(Add.to(pos, Positions.readOnly(MetasyntacticVariable.BAZ))).booleanValue());
        assertArrayEquals(MetasyntacticVariable.values(), pos.getValue());
    }

    @Test(expected = OperationException.class)
    public void testNonWritableTarget() {
        therianContext.eval(Add.to(Positions.readOnly(new String[0]), Positions.readOnly("foo")));
    }

    @Test(expected = OperationException.class)
    public void testWrongElementType() {
        therianContext
            .eval(Add.to(Positions.readWrite(String[].class, new String[0]), Positions.readOnly(new Object())));
    }

    @Test
    public void testPrimitive() {
        final Position.ReadWrite<int[]> pos = Positions.readWrite(int[].class, new int[0]);
        assertTrue(therianContext.eval(Add.to(pos, Positions.readOnly(Integer.valueOf(666)))).booleanValue());
        assertEquals(1, pos.getValue().length);
        assertEquals(666, pos.getValue()[0]);
    }
}
