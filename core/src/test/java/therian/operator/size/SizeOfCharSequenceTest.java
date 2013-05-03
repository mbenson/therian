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
package therian.operator.size;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import therian.TherianModule;
import therian.operation.Size;
import therian.operator.OperatorTest;
import therian.util.Positions;

public class SizeOfCharSequenceTest extends OperatorTest {

    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new SizeOfCharSequence());
    }

    @Test
    public void test() {
        assertEquals(0, therianContext.eval(Size.of(Positions.readOnly(String.class, null))).intValue());
        assertEquals(0, therianContext.eval(Size.of(Positions.readOnly(""))).intValue());
        assertEquals(3, therianContext.eval(Size.of(Positions.readOnly("foo"))).intValue());
        assertEquals(3, therianContext.eval(Size.of(Positions.readOnly(new StringBuilder("bar")))).intValue());
    }

}
