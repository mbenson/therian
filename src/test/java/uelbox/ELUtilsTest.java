package uelbox;

import javax.el.ELContext;
import javax.el.ExpressionFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class ELUtilsTest {
    private ELContext context;

    @Before
    public void setup() {
        context = new SimpleELContext();
    }

    @Test
    public void testGetExpressionFactory() {
        ExpressionFactory expressionFactory = ELUtils.getExpressionFactory(context);
        Assert.assertNotNull(expressionFactory);
        Assert.assertSame(expressionFactory, ELUtils.getExpressionFactory(context));
    }

    @Test
    public void testEmbedExpression() {
        Assert.assertEquals("#{foo[bar].baz}", ELUtils.embed("foo[bar].baz"));
        Assert.assertEquals("#{foo[bar].baz}", ELUtils.embed("#{foo[bar].baz}"));
        Assert.assertEquals("#{foo[bar].baz}", ELUtils.embed("${foo[bar].baz}"));
    }

    @Test
    public void testEmbedExpressionWithTrigger() {
        Assert.assertEquals("#{foo[bar].baz}", ELUtils.embed("foo[bar].baz", '#'));
        Assert.assertEquals("#{foo[bar].baz}", ELUtils.embed("#{foo[bar].baz}", '#'));
        Assert.assertEquals("#{foo[bar].baz}", ELUtils.embed("${foo[bar].baz}", '#'));
        Assert.assertEquals("!{foo[bar].baz}", ELUtils.embed("${foo[bar].baz}", '!'));
    }
}
