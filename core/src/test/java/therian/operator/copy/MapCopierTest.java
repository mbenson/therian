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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import therian.OperationException;
import therian.TherianModule;
import therian.TypeLiteral;
import therian.operation.Copy;
import therian.operator.OperatorTest;
import therian.operator.convert.ELCoercionConverter;
import therian.operator.convert.NOPConverter;
import therian.testfixture.MetasyntacticVariable;
import therian.util.Positions;

/**
 *
 */
public class MapCopierTest extends OperatorTest {
    private interface LocalTypes {
        static final TypeLiteral<Map<String, MetasyntacticVariable>> MAP_OF_STRING_TO_METASYNTACTIC_VARIABLE =
            new TypeLiteral<Map<String, MetasyntacticVariable>>() {};
        static final TypeLiteral<Map<String, String>> MAP_OF_STRING_TO_STRING =
            new TypeLiteral<Map<String, String>>() {};
        static final TypeLiteral<Map<MetasyntacticVariable, String>> MAP_OF_METASYNTACTIC_VARIABLE_TO_STRING =
            new TypeLiteral<Map<MetasyntacticVariable, String>>() {};
        static final TypeLiteral<Map<MetasyntacticVariable, MetasyntacticVariable>> MAP_OF_METASYNTACTIC_VARIABLE_TO_METASYNTACTIC_VARIABLE =
            new TypeLiteral<Map<MetasyntacticVariable, MetasyntacticVariable>>() {};
        static final TypeLiteral<Map<String, Object>> MAP_OF_STRING_TO_OBJECT =
            new TypeLiteral<Map<String, Object>>() {};
    }

    private Map<String, MetasyntacticVariable> nameToMetasyntacticVariable;

    @Before
    public void setupData() {
        nameToMetasyntacticVariable = new LinkedHashMap<String, MetasyntacticVariable>();
        for (MetasyntacticVariable mv : MetasyntacticVariable.values()) {
            nameToMetasyntacticVariable.put(mv.name(), mv);
        }
    }

    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new MapCopier(), new NOPConverter(), new ELCoercionConverter());
    }

    @Test
    public void testNoConversion() {
        final Map<String, MetasyntacticVariable> targetMap = new LinkedHashMap<String, MetasyntacticVariable>();

        assertTrue(therianContext.evalSuccess(Copy.to(
            Positions.readOnly(LocalTypes.MAP_OF_STRING_TO_METASYNTACTIC_VARIABLE, targetMap),
            Positions.readOnly(LocalTypes.MAP_OF_STRING_TO_METASYNTACTIC_VARIABLE, nameToMetasyntacticVariable))));

        assertEquals(nameToMetasyntacticVariable, targetMap);
    }

    @Test
    public void testKeyConversion() {
        final Map<MetasyntacticVariable, MetasyntacticVariable> targetMap =
            new LinkedHashMap<MetasyntacticVariable, MetasyntacticVariable>();
        assertTrue(therianContext.evalSuccess(Copy.to(
            Positions.readOnly(LocalTypes.MAP_OF_METASYNTACTIC_VARIABLE_TO_METASYNTACTIC_VARIABLE, targetMap),
            Positions.readOnly(LocalTypes.MAP_OF_STRING_TO_METASYNTACTIC_VARIABLE, nameToMetasyntacticVariable))));

        final Iterator<Map.Entry<MetasyntacticVariable, MetasyntacticVariable>> entries =
            targetMap.entrySet().iterator();
        for (MetasyntacticVariable mv : MetasyntacticVariable.values()) {
            final Map.Entry<MetasyntacticVariable, MetasyntacticVariable> e = entries.next();
            assertSame(mv, e.getKey());
            assertSame(mv, e.getValue());
        }
    }

    @Test
    public void testValueConversion() {
        final Map<String, String> targetMap = new LinkedHashMap<String, String>();
        assertTrue(therianContext.evalSuccess(Copy.to(
            Positions.readOnly(LocalTypes.MAP_OF_STRING_TO_STRING, targetMap),
            Positions.readOnly(LocalTypes.MAP_OF_STRING_TO_METASYNTACTIC_VARIABLE, nameToMetasyntacticVariable))));
        final Iterator<Map.Entry<String, String>> entries = targetMap.entrySet().iterator();
        for (MetasyntacticVariable mv : MetasyntacticVariable.values()) {
            final Map.Entry<String, String> e = entries.next();
            assertEquals(mv.name(), e.getKey());
            assertEquals(mv.name(), e.getValue());
        }
    }

    @Test
    public void testKeyAndValueConversion() {
        final Map<MetasyntacticVariable, String> targetMap = new LinkedHashMap<MetasyntacticVariable, String>();
        assertTrue(therianContext.evalSuccess(Copy.to(
            Positions.readOnly(LocalTypes.MAP_OF_METASYNTACTIC_VARIABLE_TO_STRING, targetMap),
            Positions.readOnly(LocalTypes.MAP_OF_STRING_TO_METASYNTACTIC_VARIABLE, nameToMetasyntacticVariable))));

        final Iterator<Map.Entry<MetasyntacticVariable, String>> entries = targetMap.entrySet().iterator();
        for (MetasyntacticVariable mv : MetasyntacticVariable.values()) {
            final Map.Entry<MetasyntacticVariable, String> e = entries.next();
            assertSame(mv, e.getKey());
            assertEquals(mv.name(), e.getValue());
        }
    }

    @Test(expected = OperationException.class)
    public void testUnsupportedConversion() {
        final Map<String, Object> sourceMap = new LinkedHashMap<String, Object>();
        sourceMap.put("blah", new Object());
        therianContext.eval(Copy.to(
            Positions.readOnly(LocalTypes.MAP_OF_STRING_TO_METASYNTACTIC_VARIABLE, nameToMetasyntacticVariable),
            Positions.readOnly(LocalTypes.MAP_OF_STRING_TO_OBJECT, sourceMap)));
    }

}
