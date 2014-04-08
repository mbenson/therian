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

import org.junit.Test;

import therian.TherianModule;
import therian.operation.Copy;
import therian.operator.OperatorTest;
import therian.testfixture.Address;
import therian.testfixture.Country;
import therian.util.Positions;

public class BeanCopierTest extends OperatorTest {

    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new BeanCopier());
    }

    @Test
    public void testBasic() {
        final Address source = new Address();
        source.setAddressline1("123 foo street");
        source.setAddressline2("unit 666");
        source.setCity("fooville");
        source.setZipCode("98765");
        Country country = new Country();
        country.setName("FOO.S.A.");
        country.setISO2Code("FS");
        country.setISO3Code("FSA");
        source.setCountry(country);

        final Address target = new Address();

        therianContext.eval(Copy.to(Positions.readOnly(target), Positions.readOnly(source)));
        assertEquals(source, target);
    }

    /**
     * Default behavior of a {@link PropertyCopier} should noop a null source value.
     */
    @Test
    public void testNullSource() {
        final Address control = new Address();
        final Address target = new Address();
        assertEquals(control, target);
        therianContext.eval(Copy.to(Positions.readOnly(target), Positions.readOnly(Address.class, null)));
        assertEquals(control, target);
    }
}
