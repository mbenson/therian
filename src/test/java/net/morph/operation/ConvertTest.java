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
package net.morph.operation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import net.morph.Morph;
import net.morph.MorphContext;
import net.morph.position.Constant;
import net.morph.testfixture.MetasyntacticVariable;

import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("deprecation")
public class ConvertTest {
    private MorphContext morphContext;

    @Before
    public void setup() {
//        morphContext = Morph.usingModules().context();
        morphContext = Morph.standard().context();
    }

    @Test
    public void testImmutableAssignable() {
        assertNull(morphContext.eval(new Convert<String, CharSequence>(new Constant<String>(null) {
        }, CharSequence.class)));
        assertEquals("", morphContext.eval(new Convert<String, CharSequence>(Constant.of(""), CharSequence.class)));
        assertEquals("", morphContext.eval(new Convert<String, String>(Constant.of(""), String.class)));
        assertSame(MetasyntacticVariable.FOO,
            morphContext.eval(new Convert<MetasyntacticVariable, MetasyntacticVariable>(Constant
                .of(MetasyntacticVariable.FOO), MetasyntacticVariable.class)));
        assertSame(MetasyntacticVariable.FOO, morphContext.eval(new Convert<MetasyntacticVariable, Enum<?>>(Constant
            .of(MetasyntacticVariable.FOO), Enum.class)));
        assertEquals(Integer.valueOf(666),
            morphContext.eval(new Convert<Integer, Integer>(Constant.of(Integer.valueOf(666)), Integer.class)));
        assertEquals(Integer.valueOf(666),
            morphContext.eval(new Convert<Integer, Number>(Constant.of(Integer.valueOf(666)), Number.class)));
    }

}
