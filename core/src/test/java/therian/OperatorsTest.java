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
package therian;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.hamcrest.CoreMatchers;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Assert;
import org.junit.Test;

import therian.operator.convert.ELCoercionConverter;
import therian.operator.convert.EnumToNumberConverter;
import therian.operator.immutablecheck.DefaultImmutableChecker;

public class OperatorsTest {
    public enum Success {
        SUCCESS, FAILURE;
    }

    public static abstract class Surgery extends Operation<Success> {
    }

    public static class Appendectomy extends Surgery {
    }

    public static class Tonsillectomy extends Surgery {
    }

    static class Surgeon<S extends Surgery> implements Operator<S> {

        @Override
        public boolean perform(TherianContext context, S operation) {
            operation.setResult(Success.FAILURE);
            return true;
        }

        @Override
        public boolean supports(TherianContext context, S operation) {
            return true;
        }
    }

    public static class TonsilSurgeon extends Surgeon<Tonsillectomy> {

    }

    public static class AppendixSurgeon extends Surgeon<Appendectomy> {

    }

    public static class MasterSurgeon extends Surgeon<Surgery> {

    }

    public static class SuccessOperator implements Operator<Operation<Success>> {

        @Override
        public boolean perform(TherianContext context, Operation<Success> operation) {
            return true;
        }

        @Override
        public boolean supports(TherianContext context, Operation<Success> operation) {
            return true;
        }
    }

    @Test
    public void testValidateImplementation() {
        Operators.validateImplementation(new Surgeon<Appendectomy>() {});
    }

    @Test(expected = OperatorDefinitionException.class)
    public void testValidationImplementationError() {
        Operators.validateImplementation(new Surgeon<Tonsillectomy>());
    }

    @Test
    public void testSimpleSort() {
        final Operator<?> cnv1 = new EnumToNumberConverter();
        final Operator<?> cnv2 = new ELCoercionConverter();
        assertThat((Iterable<? extends Operator<?>>) new Operators(Arrays.asList(cnv1, cnv2)), IsIterableContainingInOrder.contains(cnv1, cnv2));
        assertThat((Iterable<? extends Operator<?>>) new Operators(Arrays.asList(cnv2, cnv1)), IsIterableContainingInOrder.contains(cnv1, cnv2));

        final Operator<?> chk1 = new DefaultImmutableChecker();
        final Operator<?> chk2 = new DefaultImmutableChecker();

        assertThat((Iterable<? extends Operator<?>>) new Operators(Arrays.asList(chk1, chk2)), IsIterableContainingInOrder.contains(chk1, chk2));
        assertThat((Iterable<? extends Operator<?>>) new Operators(Arrays.asList(chk2, chk1)), IsIterableContainingInOrder.contains(chk2, chk1));
    }

    @Test
    public void testComparatorComplex() {
        final ArrayList<Operator<?>> expected = new ArrayList<Operator<?>>();
        expected.add(new AppendixSurgeon());
        expected.add(new TonsilSurgeon());
        expected.add(new MasterSurgeon());
        expected.add(new SuccessOperator());
        expected.add(new ELCoercionConverter());
        expected.add(new DefaultImmutableChecker());

        final ArrayList<Operator<?>> input = new ArrayList<Operator<?>>(expected);
        Collections.shuffle(input);

        assertThat(new Operators(input),
            IsIterableContainingInOrder.contains(expected.<Operator<?>> toArray(new Operator[expected.size()])));
    }

    @Test
    public void testStandardOperators() {
        Operators.standard();
    }
}
