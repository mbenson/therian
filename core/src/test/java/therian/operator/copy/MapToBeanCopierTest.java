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

import therian.OperationException;
import therian.TherianModule;
import therian.operation.Convert;
import therian.operation.Copy;
import therian.operator.OperatorTest;
import therian.operator.convert.DefaultCopyingConverter;
import therian.testfixture.Employee;
import therian.testfixture.MetasyntacticVariable;
import therian.util.Positions;

/**
 *
 */
public class MapToBeanCopierTest extends OperatorTest {
    public static class StringWrapper {
        final String value;

        StringWrapper(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new MapToBeanCopier(), new DefaultCopyingConverter());
    }

    @Test
    public void testCopy() {
        final Map<String, String> sourceMap = new HashMap<>();
        sourceMap.put("firstName", "Beetle");
        sourceMap.put("lastName", "Bailey");

        final Employee targetValue = new Employee();
        assertTrue(therianContext.evalSuccess(Copy.to(Positions.readOnly(targetValue),
            Positions.readOnly(new TypeLiteral<Map<String, String>>() {}, sourceMap))));

        assertEquals("Beetle", targetValue.getFirstName());
        assertNull(targetValue.getMiddleName());
        assertEquals("Bailey", targetValue.getLastName());
    }

    @Test
    public void testCopyWithReadOnlyProperty() {
        final Map<String, String> sourceMap = new HashMap<>();
        sourceMap.put("firstName", "Beetle");
        sourceMap.put("middleName", "Aloysius");
        sourceMap.put("lastName", "Bailey");

        final Employee targetValue = new Employee();
        assertTrue(therianContext.evalSuccess(Copy.to(Positions.readOnly(targetValue),
            Positions.readOnly(new TypeLiteral<Map<String, String>>() {}, sourceMap))));

        assertEquals("Beetle", targetValue.getFirstName());
        assertNull(targetValue.getMiddleName());
        assertEquals("Bailey", targetValue.getLastName());
    }

    @Test(expected = OperationException.class)
    public void testCopyWithOnlyReadOnlyProperty() {
        final Map<String, String> sourceMap = new HashMap<>();
        sourceMap.put("middleName", "Aloysius");

        final Employee targetValue = new Employee();
        therianContext.eval(Copy.to(Positions.readOnly(targetValue),
            Positions.readOnly(new TypeLiteral<Map<String, String>>() {}, sourceMap)));
    }

    @Test
    public void testCopyWithOnePropertyAndExtraneousValues() {
        final Map<String, String> sourceMap = new HashMap<>();
        sourceMap.put("firstName", "Beetle");
        for (MetasyntacticVariable mv : MetasyntacticVariable.values()) {
            sourceMap.put(mv.name(), mv.name().toLowerCase());
        }

        final Employee targetValue = new Employee();
        assertTrue(therianContext.evalSuccess(Copy.to(Positions.readOnly(targetValue),
            Positions.readOnly(new TypeLiteral<Map<String, String>>() {}, sourceMap))));

        assertEquals("Beetle", targetValue.getFirstName());
        assertNull(targetValue.getMiddleName());
        assertNull(targetValue.getLastName());
    }

    @Test
    public void testConvert() {
        final Map<String, Object> sourceMap = new HashMap<>();
        sourceMap.put("firstName", "Beetle");
        sourceMap.put("lastName", "Bailey");

        final Employee result =
            therianContext.eval(Convert.to(Employee.class,
                Positions.readOnly(new TypeLiteral<Map<String, Object>>() {}, sourceMap)));

        assertEquals("Beetle", result.getFirstName());
        assertNull(result.getMiddleName());
        assertEquals("Bailey", result.getLastName());
    }

    @Test
    public void testConvertOtherKeyType() {
        final Map<StringWrapper, StringWrapper> sourceMap = new HashMap<>();
        sourceMap.put(new StringWrapper("firstName"), new StringWrapper("Beetle"));
        sourceMap.put(new StringWrapper("lastName"), new StringWrapper("Bailey"));

        final Employee result =
            therianContext.eval(Convert.to(Employee.class,
                Positions.readOnly(new TypeLiteral<Map<StringWrapper, StringWrapper>>() {}, sourceMap)));

        assertEquals("Beetle", result.getFirstName());
        assertNull(result.getMiddleName());
        assertEquals("Bailey", result.getLastName());
    }

}
