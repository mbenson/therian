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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.functor.core.collection.FilteredIterable;
import org.apache.commons.functor.generator.IteratorToGeneratorAdapter;
import org.junit.Test;

public class OperatorsTest {
    public enum Success {
        SUCCESS, FAILURE;
    }

    public static abstract class Surgery extends Operation<Success> {
        Success result;

        @Override
        protected Success provideResult() {
            return result;
        }
    }

    public static class Appendectomy extends Surgery {
    }

    public static class Tonsillectomy extends Surgery {
    }

    static class Surgeon<S extends Surgery> implements Operator<S> {

        public void perform(S operation) {
            operation.result = Success.FAILURE;
        }

        public boolean supports(S operation) {
            return true;
        }
    }

    @Test
    public void testSupporting() {
        @SuppressWarnings("unchecked")
        final List<Surgeon<? extends Surgery>> surgeons = Arrays.asList(new Surgeon<Appendectomy>() {
        }, new Surgeon<Tonsillectomy>() {
        });
        assertEquals(
            1,
            IteratorToGeneratorAdapter
                .adapt(FilteredIterable.of(surgeons).retain(Operators.supporting(new Appendectomy())).iterator())
                .toCollection().size());
        assertEquals(
            1,
            IteratorToGeneratorAdapter
                .adapt(FilteredIterable.of(surgeons).retain(Operators.supporting(new Tonsillectomy())).iterator())
                .toCollection().size());
    }

    @Test
    public void testValidateImplementation() {
        Operators.validateImplementation(new Surgeon<Appendectomy>() {
        });
    }

    @Test(expected = OperatorDefinitionException.class)
    public void testValidationImplementationError() {
        Operators.validateImplementation(new Surgeon<Tonsillectomy>());
    }
}
