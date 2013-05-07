/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package therian.operator.copy;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import therian.TherianModule;
import therian.operation.Copy;
import therian.operator.OperatorTest;
import therian.operator.convert.ELCoercionConverter;
import therian.position.relative.Property;
import therian.position.relative.RelativePosition;
import therian.testfixture.Address;
import therian.util.Positions;

public class ConvertingCopierTest extends OperatorTest {

    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new ELCoercionConverter(), new ConvertingCopier());
    }

    @Test
    public void test() {
        final Address address = new Address();
        final RelativePosition.ReadWrite<Address, String> zipCodeOfAddress =
            Property.<String> at("zipCode").of(Positions.readOnly(address));
        therianContext.eval(Copy.to(zipCodeOfAddress, Positions.readOnly(66666)));
        assertEquals("66666", address.getZipCode());
        assertEquals("66666", zipCodeOfAddress.getValue());
    }

}
