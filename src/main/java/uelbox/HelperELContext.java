package uelbox;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.MethodExpression;
import javax.el.ValueExpression;

/**
 * Abstract "helper" ELContext:  wraps another ELContext and its associated ELResolver,
 * providing a convenience API to return results from evaluating ValueExpressions using a HelperELResolver.
 * Like any other ELContext, an instance of this class is not thread-safe.
 */
public abstract class HelperELContext<RESULT> extends ELContextWrapper {
    /**
     * Create a new HelperELContext
     *
     * @param wrapped
     */
    protected HelperELContext(ELContext wrapped) {
        super(wrapped);
    }

    /**
     * Create our HelperELResolver.
     *
     * @param elResolver
     * @return HelperELResolver
     * @see {@link ELContextWrapper#wrap(javax.el.ELResolver)}
     */
    @Override
    protected abstract HelperELResolver<RESULT> wrap(ELResolver elResolver);

    @Override
    public HelperELResolver<RESULT> getELResolver() {
        return (HelperELResolver<RESULT>) super.getELResolver();
    }

    /**
     * Return the result of evaluating valueExpression.
     *
     * @param valueExpression
     * @return RESULT
     */
    public final RESULT evaluate(ValueExpression valueExpression) {
        valueExpression.setValue(this, null);
        return getELResolver().getResult(this);
    }
}
