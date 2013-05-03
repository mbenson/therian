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
package therian.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import therian.position.Position;

/**
 *
 */
public class PositionsTest {

    @Test
    public void testConstantOfStronglyTypedObject() {
        final Position.Readable<String> pos = Positions.readOnly("foo");
        assertEquals(String.class, pos.getType());
        assertEquals("foo", pos.getValue());
    }

    @Test(expected = NullPointerException.class)
    public void testNullValueFailure() {
        Positions.readOnly(null);
    }

    @Test
    public void testNullValueWithType() {
        final Position.Readable<String> pos = Positions.readOnly(String.class, null);
        assertEquals(String.class, pos.getType());
        assertNull(pos.getValue());
    }

}
