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
package therian.uelbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import therian.uelbox.HelperELContext;
import therian.uelbox.HelperELResolver;
import therian.uelbox.SimpleELContext;
import therian.uelbox.UEL;

/**
 * Test HelperELContext / HelperELResolver.
 */
public class HelperELContextTest {
    public static class Foo {
        private Map<String, Baz> bar = new HashMap<String, Baz>();

        public Map<String, Baz> getBar() {
            return bar;
        }

        public void setBar(Map<String, Baz> bar) {
            this.bar = bar;
        }

    }

    public static class Baz {
        private Object value;

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public void go() {
            throw new UnsupportedOperationException("Shouldn't actually call me!");
        }
    }

    private ELContext context;
    private ExpressionFactory expressionFactory;
    private Foo foo;

    @Before
    public void setup() {
        foo = new Foo();
        foo.getBar().put("baz", new Baz());
        context = new SimpleELContext();
        context.getVariableMapper().setVariable("foo",
            UEL.getExpressionFactory(context).createValueExpression(foo, Foo.class));
        expressionFactory = UEL.getExpressionFactory(context);
    }

    public static class TestResult {
        final Object base;
        final List<Object> properties = new ArrayList<Object>();

        public TestResult(Object base) {
            this.base = base;
        }

    }

    @Test
    public void testHelperFunctionality() {

        HelperELContext<TestResult> helper = new HelperELContext<TestResult>(context) {

            @Override
            protected HelperELResolver<TestResult> wrap(ELResolver elResolver) {
                return new HelperELResolver.WithWorkingStorage.AsResult<TestResult>(elResolver) {

                    @Override
                    protected TestResult createWorkingStorage(ELContext context, Object base) {
                        return new TestResult(base);
                    }

                    @Override
                    protected void afterGetValue(ELContext context, Object base, Object property, Object value,
                        TestResult workingStorage) {
                        workingStorage.properties.add(property);
                    }

                    @Override
                    protected TestResult afterSetValue(ELContext context, Object base, Object property,
                        TestResult workingStorage) {
                        workingStorage.properties.add(property);
                        return workingStorage;
                    }
                };
            }
        };

        doValueExpressionTestResultAssertions(helper.evaluate(createValueExpression("foo.bar['baz'].value")), foo,
            "bar", "baz", "value");
        doValueExpressionTestResultAssertions(helper.evaluate(createValueExpression("foo.bar['baz']")), foo, "bar",
            "baz");
        doValueExpressionTestResultAssertions(helper.evaluate(createValueExpression("foo.bar")), foo, "bar");
    }

    private ValueExpression createValueExpression(String expression) {
        return createValueExpression(expression, Object.class);
    }

    private ValueExpression createValueExpression(String expression, Class<?> expectedType) {
        return expressionFactory.createValueExpression(context, UEL.embed(expression), expectedType);
    }

    private void doValueExpressionTestResultAssertions(TestResult result, Object base, Object... properties) {
        Assert.assertSame(base, result.base);
        Assert.assertEquals(Arrays.asList(properties), result.properties);
    }

}
