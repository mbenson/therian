package net.morph.operator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import net.morph.MorphModule;
import net.morph.operation.Convert;
import net.morph.position.Constant;
import net.morph.testfixture.MetasyntacticVariable;

@SuppressWarnings("deprecation")
public class ELCoercionConverterTest extends TransformerTest {

    @Override
    protected MorphModule[] modules() {
        return new MorphModule[] { MorphModule.create().withOperators(new ELCoercionConverter()) };
    }

    @Test
    public void testCoerciontoString() {
        assertEquals("666",
            morphContext.eval(new Convert<Integer, String>(Constant.of(Integer.valueOf(666)), String.class)));
    }

    @Test
    public void testCoerciontoEnum() {
        assertNull(morphContext.eval(new Convert<Object, MetasyntacticVariable>(new Constant<Object>(null) {
        }, MetasyntacticVariable.class)));
        assertNull(morphContext.eval(new Convert<String, MetasyntacticVariable>(Constant.of(""),
            MetasyntacticVariable.class)));
        assertSame(MetasyntacticVariable.FOO, morphContext.eval(new Convert<String, MetasyntacticVariable>(Constant
            .of("FOO"), MetasyntacticVariable.class)));
    }

    @Test
    public void testCoercionToBoolean() {
        assertFalse(morphContext.eval(new Convert<Object, Boolean>(new Constant<Object>(null) {
        }, Boolean.class)).booleanValue());
        assertFalse(morphContext.eval(new Convert<String, Boolean>(Constant.of(""), Boolean.class)).booleanValue());
        assertFalse(morphContext.eval(new Convert<String, Boolean>(Constant.of("false"), Boolean.class)).booleanValue());
        assertFalse(morphContext.eval(new Convert<String, Boolean>(Constant.of("whatever"), Boolean.class))
            .booleanValue());
        assertTrue(morphContext.eval(new Convert<String, Boolean>(Constant.of("true"), Boolean.class)).booleanValue());
    }

}
