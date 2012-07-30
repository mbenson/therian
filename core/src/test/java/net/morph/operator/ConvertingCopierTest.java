package net.morph.operator;

import static org.junit.Assert.assertEquals;
import net.morph.MorphModule;
import net.morph.operation.Copy;
import net.morph.position.Ref;
import net.morph.position.relative.Property;
import net.morph.position.relative.RelativePosition;
import net.morph.testfixture.Address;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

public class ConvertingCopierTest extends TransformerTest {

    @Override
    protected MorphModule[] modules() {
        return ArrayUtils.toArray(MorphModule.create().withOperators(new ELCoercionConverter(), new ConvertingCopier(),
            new DefaultImmutableChecker()));
    }

    @Test
    public void test() {
        final Address address = new Address();
        final RelativePosition.ReadWrite<Address, String> zipCodeOfAddress =
            Property.<String> at("zipCode").of(Ref.to(address));
        morphContext.eval(Copy.to(zipCodeOfAddress, Ref.to(66666)));
        assertEquals("66666", address.getZipCode());
        assertEquals("66666", zipCodeOfAddress.getValue());
    }

}
