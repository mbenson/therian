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
package net.morph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import net.morph.operation.Convert;
import net.morph.position.Constant;

import org.junit.Before;
import org.junit.Test;

public class MorphTest {
    private MorphContext morphContext;

    @Before
    public void setup() {
        morphContext = Morph.standard().context();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testImmutableAssignable() {
        assertNull(morphContext.perform(new Convert<String, CharSequence>(new Constant<String>(null) {}, CharSequence.class)));
        assertEquals("", morphContext.perform(new Convert<String, CharSequence>(Constant.of(""), CharSequence.class)));
        assertEquals("", morphContext.perform(new Convert<String, String>(Constant.of(""), String.class)));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testCoercion() {
        assertEquals("666",
            morphContext.perform(new Convert<Integer, String>(Constant.of(Integer.valueOf(666)), String.class)));
    }
}
