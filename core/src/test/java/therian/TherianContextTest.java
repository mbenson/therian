package therian;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import therian.operation.Convert;
import therian.operator.convert.ELCoercionConverter;
import therian.util.Positions;

public class TherianContextTest {

    @Test
    public void testSupports() {
        assertTrue(Therian.usingModules(TherianModule.create().withOperators(new ELCoercionConverter())).context()
            .supports(Convert.to(String.class, Positions.readOnly(Integer.valueOf(666)))));
        assertTrue(Therian.standard().context()
            .supports(Convert.to(String.class, Positions.readOnly(Integer.valueOf(666)))));
        assertFalse(Therian.standard().context()
            .supports(Convert.to(Therian.class, Positions.readOnly(Integer.valueOf(666)))));
    }

}
