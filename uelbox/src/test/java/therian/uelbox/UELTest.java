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

import javax.el.ELContext;
import javax.el.ExpressionFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import therian.uelbox.SimpleELContext;
import therian.uelbox.UEL;

/**
 * Test {@link UEL}.
 */
public class UELTest {
    private ELContext context;

    @Before
    public void setup() {
        context = new SimpleELContext();
    }

    @Test
    public void testGetExpressionFactory() {
        ExpressionFactory expressionFactory = UEL.getExpressionFactory(context);
        Assert.assertNotNull(expressionFactory);
        Assert.assertSame(expressionFactory, UEL.getExpressionFactory(context));
    }

    @Test
    public void testEmbedExpression() {
        Assert.assertEquals("#{foo[bar].baz}", UEL.embed("foo[bar].baz"));
        Assert.assertEquals("#{foo[bar].baz}", UEL.embed("#{foo[bar].baz}"));
        Assert.assertEquals("#{foo[bar].baz}", UEL.embed("${foo[bar].baz}"));
    }

    @Test
    public void testEmbedExpressionWithTrigger() {
        Assert.assertEquals("#{foo[bar].baz}", UEL.embed("foo[bar].baz", '#'));
        Assert.assertEquals("#{foo[bar].baz}", UEL.embed("#{foo[bar].baz}", '#'));
        Assert.assertEquals("#{foo[bar].baz}", UEL.embed("${foo[bar].baz}", '#'));
        Assert.assertEquals("!{foo[bar].baz}", UEL.embed("${foo[bar].baz}", '!'));
    }
}
