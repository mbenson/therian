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
import net.morph.MorphModule;
import net.morph.OperationException;
import net.morph.TypeLiteral;
import net.morph.operator.DefaultImmutableChecker;
import net.morph.position.Ref;
import net.morph.testfixture.MetasyntacticVariable;

import org.junit.Before;
import org.junit.Test;

public class ConvertTest {
    private MorphContext morphContext;

    @Before
    public void setup() {
        morphContext = Morph.usingModules(MorphModule.create().withOperators(new DefaultImmutableChecker())).context();
    }

    @Test(expected=OperationException.class)
    public void tesConverttUsingNullModules() {
        Morph morph = Morph.usingModules((MorphModule[])null);
        MorphContext morphContext = morph.context();
        assertEquals(null, morphContext.eval(Convert.to(String.class, Ref.to(""))));
    }

    @Test
    public void testImmutableAssignable() {
        TypeLiteral<CharSequence> charSequenceType = new TypeLiteral<CharSequence>() {};
        assertNull(morphContext.eval(Convert.to(charSequenceType, new Ref<String>(null) {})));
        assertEquals("", morphContext.eval(Convert.to(charSequenceType, Ref.to(""))));
        assertEquals("", morphContext.eval(Convert.to(String.class, Ref.to(""))));
        assertSame(MetasyntacticVariable.FOO,
            morphContext.eval(Convert.to(MetasyntacticVariable.class, Ref.to(MetasyntacticVariable.FOO))));
        assertSame(MetasyntacticVariable.FOO,
            morphContext.eval(Convert.to(new TypeLiteral<Enum<?>>() {}, Ref.to(MetasyntacticVariable.FOO))));
        assertEquals(Integer.valueOf(666), morphContext.eval(Convert.to(Integer.class, Ref.to(Integer.valueOf(666)))));
        assertEquals(Integer.valueOf(666), morphContext.eval(Convert.to(Number.class, Ref.to(Integer.valueOf(666)))));
    }

}
