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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.junit.Test;

import therian.TherianModule;
import therian.operation.Convert;
import therian.operation.Copy;
import therian.operator.OperatorTest;
import therian.operator.convert.CopyingConverter;
import therian.position.Position;
import therian.testfixture.Employee;
import therian.util.Positions;

/**
 *
 */
public class BeanToMapCopierTest extends OperatorTest {

    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new BeanToMapCopier(), CopyingConverter.IMPLEMENTING_MAP);
    }

    @Test
    public void testCopy() {
        final HashMap<String, Object> targetValue = new HashMap<>();
        final Position.Readable<Map<String, Object>> target =
            Positions.readOnly(new TypeLiteral<Map<String, Object>>() {}, targetValue);

        assertTrue(therianContext.evalSuccess(Copy.to(target, Positions.readOnly(new Employee("Bill", "Farmer")))));

        assertEquals(3, targetValue.size());
        assertEquals("Bill", targetValue.get("firstName"));
        assertEquals("Farmer", targetValue.get("lastName"));
        assertTrue(targetValue.containsKey("middleName"));
        assertNull(targetValue.get("middleName"));
    }

    @Test
    public void testConvert() {
        @SuppressWarnings("rawtypes")
        final Map m = therianContext.eval(Convert.to(Map.class, Positions.readOnly(new Employee("Rick", "Hunter"))));
        assertEquals(3, m.size());
        assertEquals("Rick", m.get("firstName"));
        assertEquals("Hunter", m.get("lastName"));
        assertTrue(m.containsKey("middleName"));
        assertNull(m.get("middleName"));
    }
}
