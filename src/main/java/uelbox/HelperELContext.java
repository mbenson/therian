package uelbox;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.MethodExpression;
import javax.el.ValueExpression;

/**
 * Abstract "helper" ELContext:  wraps another ELContext and its associated ELResolver,
 * and provides convenience methods to return results from evaluating expressions using a HelperELResolver.
 * Like any other ELContext, an instance of this class is not thread-safe.
 */
public abstract class HelperELContext<RESULT> extends ELContextWrapper {
    protected HelperELContext(ELContext wrapped) {
        super(wrapped);
    }

    @Override
    protected abstract HelperELResolver<RESULT> wrap(ELResolver elResolver);

    @Override
    public HelperELResolver<RESULT> getELResolver() {
        return (HelperELResolver<RESULT>) super.getELResolver();
    }

    public final RESULT evaluate(MethodExpression methodExpression) {
        methodExpression.invoke(this, null);
        return getELResolver().getResult();
    }

    public final RESULT evaluate(ValueExpression valueExpression) {
        valueExpression.setValue(this, null);
        return getELResolver().getResult();
    }
}
