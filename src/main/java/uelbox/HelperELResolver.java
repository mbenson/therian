package uelbox;

import javax.el.ELContext;
import javax.el.ELResolver;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;

/**
 * Abstract "helper" ELResolver:  handles each step in the resolution
 * of an expression, turning the ultimate set/invoke operation into a no-op,
 * and providing a result object after each such call.
 * Automatically skips intervening/nested calls between nodes of the "main" expression.
 * Not thread-safe.
 */
public abstract class HelperELResolver<RESULT> extends ELResolverWrapper {
    private static class MethodInvocationNotSupportedException extends UnsupportedOperationException {
    }

    private class State {
        final MutableInt depth = new MutableInt();
        Object tip;
        boolean complete;
        RESULT result;

        void setResult(RESULT result) {
            this.result = result;
            complete = true;
        }
    }

    private final ThreadLocal<State> currentState = new ThreadLocal<State>() {
        @Override
        protected State initialValue() {
            return new State();
        }
    };

    protected HelperELResolver(ELResolver wrapped) {
        super(wrapped);
    }

    @Override
    public final Object getValue(ELContext context, Object base, Object property) {
        State state;
        synchronized (this) {
            state = currentState.get();
            if (state.complete) {
                currentState.remove();
                state = currentState.get();
            }
        }
        Object value = super.getValue(context, base, property);

        // deal with the (unusual) case that we never receive a read against a null base:
        if (state.tip == null && base != null) {
            state.tip = base;
        }

        // look for nested reads against tip:
        // e.g. base.aList[base.aList.size() - 1]
        if (value == state.tip) {
            if (base == value) {
                // somewhat odd, but value seems to be its own property, so simply skip this node:
            } else {
                // record that we expect to see a property resolved against tip before we pick up recording again:
                state.depth.increment();
            }
        } else if (base == state.tip) {
            // if applicable, pop out of the most recent nested read against tip:
            if (state.depth.intValue() > 0) {
                state.depth.decrement();
            } else {
                try {
                    afterGetValue(context, base, property, value);
                } finally {
                    state.tip = value;
                }
            }
        }
        return value;
    }

    /**
     * Method invocation support is perhaps a little shaky, due to the fact that any
     * non-nested method invocation is assumed to be the final one.  The result of this is that
     * {@link #afterInvoke(javax.el.ELContext, Object, Object, Class[], Object[])} may be invoked more than once.
     *
     * @param context
     * @param base
     * @param method
     * @param paramTypes
     * @param params
     * @return result of calling the method against the wrapped ELResolver for nested invocations; {@code null}
     *         otherwise.
     */
    @Override
    public final Object invoke(ELContext context, Object base, Object method, Class<?>[] paramTypes, Object[] params) {
        State state = currentState.get();
        // behave normally for internal/nested method invocations:
        if (base == state.tip && state.depth.intValue() == 0) {
            try {
                state.setResult(afterInvoke(context, base, method, paramTypes, params));
                return null;
            } catch (MethodInvocationNotSupportedException e) {
                // swallow, assume this helper is intended for valueExpressions
            }
        }
        return super.invoke(context, base, method, paramTypes, params);
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) {
        State state = currentState.get();
        Validate.validState(base == state.tip && state.depth.intValue() == 0);
        state.setResult(afterSetValue(context, base, property));
    }

    /**
     * Post-process {@link #getValue(javax.el.ELContext, Object, Object)}.  Default no-op.
     *
     * @param context
     * @param base
     * @param property
     * @param value
     */
    protected void afterGetValue(ELContext context, Object base, Object property, Object value) {
    }

    /**
     * Post-process {@link #setValue(javax.el.ELContext, Object, Object, Object)}.
     *
     * @param context
     * @param base
     * @param property
     * @return RESULT
     * @throws UnsupportedOperationException by default
     */
    protected RESULT afterSetValue(ELContext context, Object base, Object property) {
        throw new UnsupportedOperationException();
    }

    /**
     * Post-process {@link #invoke(javax.el.ELContext, Object, Object, Class[], Object[])}.  Default
     * behavior allows the method invocation to proceed as usual.
     *
     * @param context
     * @param base
     * @param method
     * @param paramTypes
     * @param params
     * @return RESULT
     */
    protected RESULT afterInvoke(ELContext context, Object base, Object method, Class<?>[] paramTypes,
                                 Object[] params) {
        throw new MethodInvocationNotSupportedException();
    }

    /**
     * Get result. Available only once per context operation.
     *
     * @return RESULT
     */
    public final RESULT getResult() {
        State state = currentState.get();
        Validate.validState(state != null && state.complete);
        try {
            return state.result;
        } finally {
            currentState.remove();
        }
    }

}
