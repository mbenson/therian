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
package therian.operator.getelementtype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.junit.Test;

import therian.TherianModule;
import therian.operation.GetElementType;
import therian.operator.OperatorTest;

public class GetArrayElementTypeTest extends OperatorTest {

    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new GetArrayElementType());
    }

    @Test
    public void test() {
        assertEquals(int.class, therianContext.eval(GetElementType.of(new TypeLiteral<int[]>() {})));
        assertEquals(String.class, therianContext.eval(GetElementType.of(new TypeLiteral<String[]>() {})));
        assertTrue(TypeUtils.equals(new TypeLiteral<List<String>>() {}.value,
            therianContext.eval(GetElementType.of(new TypeLiteral<List<String>[]>() {}))));
        assertTrue(TypeUtils.equals(new TypeLiteral<Object[]>() {}.value,
            therianContext.eval(GetElementType.of(new TypeLiteral<Object[][]>() {}))));
    }

}
