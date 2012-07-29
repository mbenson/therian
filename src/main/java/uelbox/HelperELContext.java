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
package uelbox;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ValueExpression;

/**
 * Abstract "helper" ELContext: wraps another ELContext and its associated ELResolver, providing a convenience API to
 * return results from evaluating ValueExpressions using a HelperELResolver. Like any other ELContext, an instance of
 * this class is not thread-safe.
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
        @SuppressWarnings("unchecked")
        final HelperELResolver<RESULT> result = (HelperELResolver<RESULT>) super.getELResolver();
        return result;
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
