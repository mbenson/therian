package therian.position.relative;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import therian.position.Ref;
import therian.testfixture.MetasyntacticVariable;

public class KeyedValueTest {
    private Map<String, MetasyntacticVariable> map;
    RelativePosition.ReadWrite<Map<String, MetasyntacticVariable>, MetasyntacticVariable> atFoo;

    @Before
    public void setup() {
        map = new LinkedHashMap<String, MetasyntacticVariable>();
        for (MetasyntacticVariable var : MetasyntacticVariable.values()) {
            map.put(var.name().toLowerCase(Locale.US), var);
        }
        atFoo = Keyed.<MetasyntacticVariable> value().at("foo").of(new Ref<Map<String, MetasyntacticVariable>>(map) {});
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
}
