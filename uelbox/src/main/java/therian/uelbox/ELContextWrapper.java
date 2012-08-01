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

import java.util.Locale;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.FunctionMapper;
import javax.el.ValueExpression;
import javax.el.VariableMapper;

import org.apache.commons.lang3.Validate;

/**
 * ELContext wrapper which wraps the ELResolver and may shadow variables, locale settings, and context objects.
 */
public abstract class ELContextWrapper extends ELContext {
    private final ELResolver elResolver;
    private final VariableMapper variableMapper;
    protected final ELContext wrapped;

    /**
     * Create a new ELContextWrapper.
     * 
     * @param wrapped
     */
    protected ELContextWrapper(ELContext wrapped) {
        this.wrapped = Validate.notNull(wrapped, "wrapped ELContext");
        this.elResolver = Validate.notNull(wrap(wrapped.getELResolver()));
        this.variableMapper = new SimpleVariableMapper() {
            @Override
            public ValueExpression resolveVariable(String variable) {
                if (containsVariable(variable)) {
                    return super.resolveVariable(variable);
                }
                return ELContextWrapper.this.wrapped.getVariableMapper().resolveVariable(variable);
            }
        };
    }

    /**
     * Create a wrapped ELResolver for use with the wrapped {@link ELContext}.
     * 
     * @param elResolver
     *            to wrap
     * @return {@link ELResolver}
     */
    protected abstract ELResolver wrap(ELResolver elResolver);

    @Override
    public ELResolver getELResolver() {
        return elResolver;
    }

    @Override
    public FunctionMapper getFunctionMapper() {
        return wrapped.getFunctionMapper();
    }

    @Override
    public VariableMapper getVariableMapper() {
        return variableMapper;
    }

    @Override
    public Locale getLocale() {
        final Locale result = super.getLocale();
        return result == null ? super.getLocale() : result;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Object getContext(Class key) {
        final Object result = super.getContext(key);
        return result == null ? wrapped.getContext(key) : result;
    }

    /**
     * Convenience method to return a typed context object when key resolves per documented convention to an object of
     * the same type.
     * 
     * @param key
     * @param <T>
     * @return T
     * @see ELContext#getContext(Class)
     */
    public final <T> T getTypedContext(Class<T> key) {
        return UEL.getContext(this, key);
    }

}
