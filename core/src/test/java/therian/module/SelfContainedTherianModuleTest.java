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
package therian.module;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import therian.Operation;
import therian.Operator;
import therian.Operator.DependsOn;
import therian.Therian;
import therian.TherianContext;
import therian.operation.Convert;
import therian.operator.convert.ELCoercionConverter;
import therian.operator.convert.NOPConverter;
import therian.testfixture.MetasyntacticVariable;
import therian.util.Positions;
import therian.util.Types.Interfaces;

/**
 *
 */
public class SelfContainedTherianModuleTest {
    public static abstract class Op extends Operation<MetasyntacticVariable> {
        private MetasyntacticVariable result;

        public void setResult(MetasyntacticVariable result) {
            this.result = result;
        }

        @Override
        protected MetasyntacticVariable provideResult() {
            return result;
        }
    }

    public static class Foo extends Op {
    }

    public static class Bar extends Op {
    }

    public static class Baz extends Op {
    }

    public static class FooDep extends Op {
    }

    public static class BarDep extends Op {
    }

    public static class BazDep extends Op {
    }

    public static class Blah extends Op {
    }

    public static class BlahOperatorWithPrivateConstructor implements Operator<Blah> {

        private BlahOperatorWithPrivateConstructor() {
        }

        @Override
        public boolean perform(TherianContext context, Blah op) {
            op.setResult(null);
            return true;
        }

        @Override
        public boolean supports(TherianContext context, Blah operation) {
            return true;
        }

    }

    public static class FooDepOperator implements Operator<FooDep> {

        @Override
        public boolean perform(TherianContext context, FooDep op) {
            op.setResult(MetasyntacticVariable.FOO);
            return true;
        }

        @Override
        public boolean supports(TherianContext context, FooDep op) {
            return true;
        }

    }

    public static class BarDepOperator implements Operator<BarDep> {

        @Override
        public boolean perform(TherianContext context, BarDep op) {
            op.setResult(MetasyntacticVariable.BAR);
            return true;
        }

        @Override
        public boolean supports(TherianContext context, BarDep op) {
            return true;
        }

    }

    public static class BazDepOperator implements Operator<BazDep> {

        @Override
        public boolean perform(TherianContext context, BazDep op) {
            op.setResult(MetasyntacticVariable.BAZ);
            return true;
        }

        @Override
        public boolean supports(TherianContext context, BazDep op) {
            return true;
        }

    }

    public static abstract class AbstractModule extends SelfContainedTherianModule {
        @DependsOn(FooDepOperator.class)
        public static class FooOperator implements Operator<Foo> {

            @Override
            public boolean perform(TherianContext context, Foo op) {
                op.setResult(MetasyntacticVariable.FOO);
                return true;
            }

            @Override
            public boolean supports(TherianContext context, Foo op) {
                return context.supports(new FooDep());
            }

        }

        protected AbstractModule(Interfaces interfacesPolicy) {
            super(interfacesPolicy);
        }
    }

    public static interface ImplementsBar {
        @DependsOn(BarDepOperator.class)
        public static class BarOperator implements Operator<Bar> {

            @Override
            public boolean perform(TherianContext context, Bar op) {
                op.setResult(MetasyntacticVariable.BAR);
                return true;
            }

            @Override
            public boolean supports(TherianContext context, Bar op) {
                return context.supports(new BarDep());
            }

        }
    }

    public static class Module extends AbstractModule implements ImplementsBar {
        /**
         * BazOperator is non-static, yet functions... this could have interesting implications for operator/module
         * interactivity.
         */
        @DependsOn(BazDepOperator.class)
        public class BazOperator implements Operator<Baz> {

            @Override
            public boolean perform(TherianContext context, Baz op) {
                op.setResult(MetasyntacticVariable.BAZ);
                return true;
            }

            @Override
            public boolean supports(TherianContext context, Baz op) {
                return context.supports(new BazDep());
            }

        }

        public static abstract class AbstractBlahOperator implements Operator<Blah> {
        }

        public interface BlahOperatorInterface extends Operator<Blah> {
        }

        @SuppressWarnings("unused")
        private static class PrivateBlahOperator implements Operator<Blah> {
            public PrivateBlahOperator() {
            }

            @Override
            public boolean perform(TherianContext context, Blah op) {
                op.setResult(null);
                return true;
            }

            @Override
            public boolean supports(TherianContext context, Blah operation) {
                return true;
            }
        }

        public Module(Interfaces interfacesPolicy) {
            super(interfacesPolicy);
            withOperators(new NOPConverter(), new ELCoercionConverter());
        }

    }

    @Test
    public void testWithoutInterfaces() {
        final TherianContext context = Therian.usingModules(new Module(Interfaces.EXCLUDE)).context();

        assertSame("superclass-defined operator", MetasyntacticVariable.FOO, context.eval(new Foo()));
        assertSame("superclass-defined operator dependency", MetasyntacticVariable.FOO, context.eval(new FooDep()));

        assertFalse("excluded interface-defined operator", context.supports(new Bar()));
        assertFalse("excluded interface-defined operator dependency", context.supports(new BarDep()));

        assertSame("locally defined operator", MetasyntacticVariable.BAZ, context.eval(new Baz()));
        assertSame("locally defined operator dependency", MetasyntacticVariable.BAZ, context.eval(new BazDep()));

        assertFalse("non-instantiable operators", context.supports(new Blah()));

        assertEquals("ELCoercionConverter specified by TherianModule#withOperators()", MetasyntacticVariable.FOO,
            context.eval(Convert.to(MetasyntacticVariable.class, Positions.readOnly("FOO"))));

        assertEquals("NOPConverter specified by TherianModule#withOperators()", MetasyntacticVariable.FOO,
            context.eval(Convert.to(MetasyntacticVariable.class, Positions.readOnly(MetasyntacticVariable.FOO))));
    }

    @Test
    public void testWithInterfaces() {
        final TherianContext context = Therian.usingModules(new Module(Interfaces.INCLUDE)).context();

        assertSame("superclass-defined operator", MetasyntacticVariable.FOO, context.eval(new Foo()));
        assertSame("superclass-defined operator dependency", MetasyntacticVariable.FOO, context.eval(new FooDep()));

        assertSame("interface-defined operator", MetasyntacticVariable.BAR, context.eval(new Bar()));
        assertSame("interface-defined operator dependency", MetasyntacticVariable.BAR, context.eval(new BarDep()));

        assertSame("locally defined operator", MetasyntacticVariable.BAZ, context.eval(new Baz()));
        assertSame("locally defined operator dependency", MetasyntacticVariable.BAZ, context.eval(new BazDep()));

        assertFalse("non-instantiable operators", context.supports(new Blah()));

        assertEquals("ELCoercionConverter specified by TherianModule#withOperators()", MetasyntacticVariable.FOO,
            context.eval(Convert.to(MetasyntacticVariable.class, Positions.readOnly("FOO"))));

        assertEquals("NOPConverter specified by TherianModule#withOperators()", MetasyntacticVariable.FOO,
            context.eval(Convert.to(MetasyntacticVariable.class, Positions.readOnly(MetasyntacticVariable.FOO))));
    }

}
