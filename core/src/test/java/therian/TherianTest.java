package therian;

import static org.junit.Assert.*;

import org.junit.Test;

import therian.operation.Convert;
import therian.position.Ref;

public class TherianTest {

    @Test
    public void testSupports() {
        assertTrue(Therian.standard().supports(Convert.to(String.class, Ref.to(Integer.valueOf(666)))));
        assertFalse(Therian.standard().supports(Convert.to(Therian.class, Ref.to(Integer.valueOf(666)))));
    }

}
