/*
 *  Copyright the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package therian.operator.copy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import therian.OperationException;
import therian.TherianContext.Hint;
import therian.TherianModule;
import therian.operation.Copy;
import therian.operator.OperatorTest;
import therian.operator.copy.PropertyCopier.NullBehavior;
import therian.testfixture.Address;
import therian.testfixture.Country;
import therian.util.Positions;

/**
 * Also tests some {@link PropertyCopier} functionality, e.g. matching and NullBehavior.
 */
public class BeanCopierTest extends OperatorTest {
    private Address fullAddress;
    private Address emptyAddress;

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
        emptyAddress = new Address();
        fullAddress = new Address();
        fullAddress.setAddressline1("123 foo street");
        fullAddress.setAddressline2("unit 666");
        fullAddress.setCity("fooville");
        fullAddress.setZipCode("98765");
        Country country = new Country();
        country.setName("FOO.S.A.");
        country.setISO2Code("FS");
        country.setISO3Code("FSA");
        fullAddress.setCountry(country);
    }

    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new BeanCopier());
    }

    @Test
    public void testBasic() {
        final Address target = new Address();
        therianContext.eval(Copy.to(Positions.readOnly(target), Positions.readOnly(fullAddress)));
        assertEquals(fullAddress, target);
    }

    /**
     * Default behavior of a {@link PropertyCopier} should noop a null source value.
     */
    @Test
    public void testNullSource() {
        final Address target = new Address();
        assertEquals(emptyAddress, target);
        therianContext.eval(Copy.to(Positions.readOnly(target), Positions.readOnly(Address.class, null)));
        assertEquals(emptyAddress, target);
    }

    @Test
    public void testNullSourceSetNulls() {
        therianContext.eval(Copy.to(Positions.readOnly(fullAddress), Positions.readOnly(Address.class, null)),
            Arrays.<Hint> asList(NullBehavior.COPY_NULLS));

        assertNull(fullAddress.getAddressline1());
        assertNull(fullAddress.getAddressline2());
        assertNull(fullAddress.getCity());
        assertNull(fullAddress.getZipCode());
        assertNotNull(fullAddress.getCountry());
        assertNull(fullAddress.getCountry().getName());
        assertNull(fullAddress.getCountry().getISO2Code());
        assertNull(fullAddress.getCountry().getISO3Code());
    }

    @Test(expected = OperationException.class)
    public void testNullSourceUnsupportedHint() {
        therianContext.eval(Copy.to(Positions.readOnly(new Address()), Positions.readOnly(Address.class, null)),
            NullBehavior.UNSUPPORTED);
    }

}
