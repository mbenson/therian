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
package therian.position.relative;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.junit.Before;
import org.junit.Test;

import therian.position.Position;
import therian.testfixture.MetasyntacticVariable;
import therian.util.Positions;

public class KeyedValueTest {
    private Map<String, MetasyntacticVariable> map;
    RelativePosition.ReadWrite<Map<String, MetasyntacticVariable>, MetasyntacticVariable> atFoo;

    @Before
    public void setup() {
        map = new LinkedHashMap<String, MetasyntacticVariable>();
        for (MetasyntacticVariable var : MetasyntacticVariable.values()) {
            map.put(var.name().toLowerCase(Locale.US), var);
        }
        atFoo =
            Keyed.<MetasyntacticVariable> value().at("foo")
                .of(Positions.readOnly(new TypeLiteral<Map<String, MetasyntacticVariable>>() {}, map));
    }

    @Test
    public void testGetType() {
        assertEquals(MetasyntacticVariable.class, atFoo.getType());
    }

    @Test
    public void testGetValue() {
        assertEquals(MetasyntacticVariable.FOO, atFoo.getValue());
    }

    @Test
    public void testSetValue() {
        atFoo.setValue(null);
        assertNull(map.get("foo"));
        atFoo.setValue(MetasyntacticVariable.BAR);
        assertSame(MetasyntacticVariable.BAR, map.get("foo"));
    }

    @Test
    public void testToString() {
        final Position.Readable<HashMap<String, Object>> mapRef = Positions.readOnly(new HashMap<String, Object>());
        assertEquals(String.format("Relative Position: Keyed Value [foo] of %s", mapRef),
            Keyed.value().at("foo").of(mapRef).toString());
    }
}
