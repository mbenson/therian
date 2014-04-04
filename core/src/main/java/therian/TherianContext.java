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
package therian;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.el.ELContext;
import javax.el.ELResolver;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;

import therian.OperatorManager.SupportChecker;
import therian.el.TherianContextELResolver;
import therian.uelbox.ELContextWrapper;
import therian.util.ReadOnlyUtils;

/**
 * Therian context.
 */
public class TherianContext extends ELContextWrapper {
    /**
     * Callback interface.
     *
     * @param <T>
     */
    public interface Callback<T> {
        void handle(T arg);
    }

    /**
     * Job interface.
     *
     * @param <T>
     */
    public interface Job<T> {
        T evaluate(TherianContext context);
    }

    /**
     * Generalizes a hint targeted to some {@link Operator} that can be set on the context.
     *
     * @see TherianContext#doWithHints(UnaryFunction, Hint...)
     */
    public static abstract class Hint {
        /**
         * By default, {@link #getClass()}
         *
         * @return Class, of which {@code this} must be an instance
         */
        protected Class<? extends Hint> getTypeImpl() {
            return getClass();
        }

        /**
         * Get the hint type to use.
         *
         * @return Class
         */
        public final Class<? extends Hint> getType() {
            final Class<? extends Hint> result = getTypeImpl();
            Validate.validState(result.isInstance(this), "%s is not an instance of %s", this, result);
            return result;
        }
    }

    /**
     * Nested {@link ELContextWrapper} that wraps what this {@link TherianContext} wraps, which can be used with
     * "utility" {@link ELResolver} wrappers.
     */
    public abstract class NestedELContextWrapper extends ELContextWrapper {

        public NestedELContextWrapper() {
            super(TherianContext.this.wrapped);
        }
    }

    private static final Logger LOG = Logger.getLogger(TherianContext.class.getName());
    private static final ThreadLocal<TherianContext> CURRENT_INSTANCE = new ThreadLocal<TherianContext>();

    private final Deque<Operation<?>> operations = new ArrayDeque<Operation<?>>();
    private final SupportChecker supportChecker;

    TherianContext(ELContext wrapped, OperatorManager operatorManager) {
        super(wrapped);
        supportChecker = Validate.notNull(operatorManager, "operatorManager").new SupportChecker(this);
    }

    @Override
    protected ELResolver wrap(ELResolver elResolver) {
        return new TherianContextELResolver(elResolver);
    }

    /**
     * Get current thread-bound instance.
     *
     * @return {@link TherianContext} or {@code null}
     */
    private static TherianContext getCurrentInstance() {
        return CURRENT_INSTANCE.get();
    }

    /**
     * Get some usable {@link TherianContext} instance.
     *
     * @return current thread-bound instance or {@code Therian.standard().context()}
     */
    public static TherianContext getInstance() {
        final TherianContext current = getCurrentInstance();
        if (current != null) {
            return current;
        }
        return Therian.standard().context();
    }

    /**
     * Return the result of evaluating {@code function} against {@code this} with {@code hints} specified for the
     * duration.
     *
     * @param function
     * @param hints
     * @return T
     */
    public synchronized <T> T doWithHints(Job<T> function, Hint... hints) {
        Validate.notNull(function, "function");
        Validate.noNullElements(hints, "null element at hints[%s]");

        final Map<Class<? extends Hint>, Hint> localHints = new HashMap<Class<? extends Hint>, TherianContext.Hint>();
        for (Hint hint : hints) {
            final Class<? extends Hint> key = hint.getType();
            if (localHints.containsKey(key)) {
                throw new IllegalArgumentException(String.format("Found hints [%s, %s] with same type %s",
                    localHints.get(key), hint, key));
            }
            localHints.put(key, hint);
        }
        final Map<Class<? extends Hint>, Hint> restoreHints = new HashMap<Class<? extends Hint>, TherianContext.Hint>();
        for (final Iterator<Map.Entry<Class<? extends Hint>, Hint>> entries = localHints.entrySet().iterator(); entries
            .hasNext();) {
            final Map.Entry<Class<? extends Hint>, Hint> e = entries.next();
            final Hint existingHint = getTypedContext(e.getKey());
            if (e.getValue().equals(existingHint)) {
                entries.remove();
            } else {
                restoreHints.put(e.getKey(), existingHint);
            }
        }
        for (Map.Entry<Class<? extends Hint>, Hint> e : localHints.entrySet()) {
            putContext(e.getKey(), e.getValue());
        }

        try {
            return function.evaluate(this);
        } finally {
            for (Map.Entry<Class<? extends Hint>, Hint> e : localHints.entrySet()) {
                putContext(e.getKey(), restoreHints.get(e.getKey()));
            }
        }
    }

    /**
     * Learn whether {@code operation} is supported by this context.
     *
     * @param operation
     * @return boolean
     * @throws NullPointerException on {@code null} input
     */
    public synchronized boolean supports(final Operation<?> operation) {
        Validate.notNull(operation, "operation");
        final TherianContext originalContext = getCurrentInstance();
        if (originalContext != this) {
            CURRENT_INSTANCE.set(this);
        }
        try {
            final Iterator<Operator<?>> supportingOperators = supportChecker.operatorsSupporting(operation).iterator();
            return supportingOperators.hasNext();
        } finally {
            if (originalContext == null) {
                CURRENT_INSTANCE.remove();
            } else if (originalContext != this) {
                // restore original context in the unlikely event that multiple contexts are being used on the same
                // thread:
                CURRENT_INSTANCE.set(originalContext);
            }
        }
    }

    /**
     * Evaluates {@code operation} if supported; otherwise returns {@code null}. You may distinguish between a
     * {@code null} result and "not supported" by calling {@link #supports(Operation)} and {@link #eval(Operation)}
     * independently.
     *
     * @param operation
     * @return RESULT or {@code null}
     * @throws NullPointerException on {@code null} input
     * @throws OperationException potentially, via {@link Operation#getResult()}
     */
    public final synchronized <RESULT, OPERATION extends Operation<RESULT>> RESULT evalIfSupported(OPERATION operation) {
        return evalIfSupported(operation, null);
    }

    /**
     * Evaluates {@code operation} if supported; otherwise returns {@code defaultValue}.
     *
     * @param operation
     * @param defaultValue
     * @return RESULT or {@code null}
     * @throws NullPointerException on {@code null} input
     * @throws OperationException potentially, via {@link Operation#getResult()}
     */
    public final synchronized <RESULT, OPERATION extends Operation<RESULT>> RESULT evalIfSupported(OPERATION operation,
        RESULT defaultValue) {
        return supports(operation) ? eval(operation) : defaultValue;
    }

    /**
     * Convenience method to perform an operation, discarding its result, and report whether it succeeded.
     *
     * @param operation
     * @return whether {@code operation} was supported and successful
     * @throws NullPointerException on {@code null} input
     * @throws OperationException potentially, via {@link Operation#getResult()}
     */
    public final synchronized boolean evalSuccess(Operation<?> operation) {
        if (!supports(operation)) {
            return false;
        }
        eval(operation);
        return operation.isSuccessful();
    }

    /**
     * Performs the specified {@link Operation} by invoking any compatible {@link Operator} until the {@link Operation}
     * is marked as having been successful, then returns the result from {@link Operation#getResult()}. Note that
     * <em>most</em> unsuccessful {@link Operation}s will, at this point, throw an {@link OperationException}.
     *
     * @param operation
     * @param <RESULT>
     * @param <OPERATION>
     * @return result if available
     * @throws NullPointerException on {@code null} input
     * @throws OperationException potentially, via {@link Operation#getResult()}
     */
    public final synchronized <RESULT, OPERATION extends Operation<RESULT>> RESULT eval(OPERATION operation) {
        Validate.notNull(operation, "operation");
        final TherianContext originalContext = getCurrentInstance();
        if (originalContext != this) {
            CURRENT_INSTANCE.set(this);
        }
        try {
            if (operations.contains(operation)) {
                for (Operation<?> op : operations) {
                    if (op.equals(operation) && op.isSuccessful()) {
                        @SuppressWarnings("unchecked")
                        RESULT result = (RESULT) op.getResult();
                        return result;
                    }
                }
                throw new OperationException(operation, "recursive operation detected");
            }
            operations.push(operation);

            final Iterable<Operator<?>> supportingOperators = supportChecker.operatorsSupporting(operation);

            try {
                for (Operator<?> operator : supportingOperators) {
                    if (evalRaw(operation, operator)) {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine(String.format("Successfully evaluated %s with operator %s", operation, operator));
                        }
                        operation.setSuccessful(true);
                        break;
                    }
                }
                return operation.getResult();
            } finally {
                final Operation<?> opOnPop = operations.pop();
                Validate.validState(opOnPop == operation,
                    "operation stack out of whack; found %s where %s was expected", opOnPop, operation);
            }
        } finally {
            if (originalContext == null) {
                CURRENT_INSTANCE.remove();
            } else if (originalContext != this) {
                // restore original context in the unlikely event that multiple contexts are being used on the same
                // thread:
                CURRENT_INSTANCE.set(originalContext);
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private boolean evalRaw(Operation operation, Operator operator) {
        return operator.perform(this, operation);
    }

    /**
     * Delegate the success of the current operation to that of another.
     *
     * @param operation
     * @param callback
     * @param <RESULT>
     * @param <OPERATION>
     * @return operation's success
     */
    public final synchronized <RESULT> boolean forwardTo(final Operation<RESULT> operation,
        final Callback<? super RESULT> callback) {
        Validate.validState(!operations.isEmpty(), "cannot forward without an ongoing operation");
        final Operation<?> owner = operations.peek();
        Validate
            .isTrue(ObjectUtils.notEqual(owner, operation), "operations %s and %s are same/equal", owner, operation);
        final RESULT result = eval(operation);
        if (operation.isSuccessful()) {
            if (callback != null) {
                callback.handle(result);
            }
            return true;
        }
        return false;
    }

    /**
     * Return a read-only view of the current operations stack.
     *
     * @return Deque<Operation<?>>
     */
    public Deque<Operation<?>> getOperations() {
        return ReadOnlyUtils.wrap(operations);
    }

}
