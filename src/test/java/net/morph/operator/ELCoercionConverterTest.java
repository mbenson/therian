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
package net.morph.operator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import net.morph.MorphModule;
import net.morph.operation.Convert;
import net.morph.position.Ref;
import net.morph.testfixture.MetasyntacticVariable;

@SuppressWarnings("deprecation")
public class ELCoercionConverterTest extends TransformerTest {

    @Override
    protected MorphModule[] modules() {
        return new MorphModule[] { MorphModule.create().withOperators(new ELCoercionConverter()) };
    }

    @Test
    public void testCoerciontoString() {
        assertEquals("666", morphContext.eval(new Convert<Integer, String>(Ref.to(Integer.valueOf(666)), String.class)));
    }

    @Test
    public void testCoerciontoEnum() {
        assertNull(morphContext.eval(new Convert<Object, MetasyntacticVariable>(new Ref<Object>(null) {},
            MetasyntacticVariable.class)));
        assertNull(morphContext
            .eval(new Convert<String, MetasyntacticVariable>(Ref.to(""), MetasyntacticVariable.class)));
        assertSame(MetasyntacticVariable.FOO,
            morphContext.eval(new Convert<String, MetasyntacticVariable>(Ref.to("FOO"), MetasyntacticVariable.class)));
    }

    @Test
    public void testCoercionToBoolean() {
        assertFalse(morphContext.eval(new Convert<Object, Boolean>(new Ref<Object>(null) {}, Boolean.class))
            .booleanValue());
        assertFalse(morphContext.eval(new Convert<String, Boolean>(Ref.to(""), Boolean.class)).booleanValue());
        assertFalse(morphContext.eval(new Convert<String, Boolean>(Ref.to("false"), Boolean.class)).booleanValue());
        assertFalse(morphContext.eval(new Convert<String, Boolean>(Ref.to("whatever"), Boolean.class)).booleanValue());
        assertTrue(morphContext.eval(new Convert<String, Boolean>(Ref.to("true"), Boolean.class)).booleanValue());
    }

}
