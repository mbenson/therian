package uelbox;

import javax.el.ELContext;
import javax.el.ExpressionFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
