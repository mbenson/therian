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

import javax.el.ELContext;
import javax.el.ELResolver;

import org.apache.commons.functor.UnaryPredicate;
import org.apache.commons.functor.UnaryProcedure;
import org.apache.commons.functor.core.collection.FilteredIterable;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import therian.el.TherianContextELResolver;
import therian.uelbox.ELContextWrapper;
import therian.util.ReadOnlyUtils;

/**
 * Therian context.
 */
public class TherianContext extends ELContextWrapper {
    private class OperatorFilter implements UnaryPredicate<Operator<?>> {
        private final Operation<?> operation;

        private OperatorFilter(Operation<?> operation) {
            this.operation = Validate.notNull(operation);
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        public boolean test(Operator operator) {
            final Pair<Operation<?>, Operator<?>> pair =
                ImmutablePair.<Operation<?>, Operator<?>> of(operation, operator);
            if (!supportChecks.contains(pair) && operation.matches(operator)) {
                supportChecks.push(pair);
                try {
                    return operator.supports(TherianContext.this, operation);
                } finally {
                    Validate.validState(supportChecks.pop() == pair, "supportChecks out of whack");
                }
            }
            return false;
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

    private static final ThreadLocal<TherianContext> CURRENT_INSTANCE = new ThreadLocal<TherianContext>();

    private final Deque<Operation<?>> operations = new ArrayDeque<Operation<?>>();
    private final Deque<Pair<Operation<?>, Operator<?>>> supportChecks =
        new ArrayDeque<Pair<Operation<?>, Operator<?>>>();

    TherianContext(ELContext wrapped) {
        super(wrapped);
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
            return FilteredIterable.of(getTypedContext(Therian.class).getOperators())
                .retain(new OperatorFilter(operation)).iterator().hasNext();
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
        return supports(operation) ? eval(operation) : null;
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
     * @return boolean
     * @throws NullPointerException on {@code null} input
     * @throws OperationException potentially, via {@link Operation#getResult()}
     */
    public final synchronized boolean evalSuccess(Operation<?> operation) {
        eval(operation);
        return operation.isSuccessful();
    }

    /**
     * Convenience method to perform an operation, discarding its result, and report whether it succeeded.
     *
     * @param operation
     * @return boolean
     * @throws NullPointerException on {@code null} input
     * @throws OperationException potentially, via {@link Operation#getResult()}
     * @see #supports(Operation)
     */
    public final synchronized boolean evalSuccessIfSupported(Operation<?> operation) {
        return supports(operation) && evalSuccess(operation);
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

            final FilteredIterable<Operator<?>> applicableOperators =
                FilteredIterable.of(getTypedContext(Therian.class).getOperators())
                    .retain(new OperatorFilter(operation));

            try {
                for (Operator<?> operator : applicableOperators) {
                    if (evalRaw(operation, operator)) {
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
     * @param <RESULT>
     * @param <OPERATION>
     * @return operation's success
     */
    public final synchronized boolean forwardTo(final Operation<?> operation) {
        return forwardTo(operation, null);
    }

    /**
     * Delegate the success of the current operation to that of another.
     *
     * @param operation
     * @param <RESULT>
     * @param <OPERATION>
     * @return operation's success
     */
    public final synchronized <RESULT> boolean forwardTo(final Operation<RESULT> operation,
        final UnaryProcedure<? super RESULT> callback) {
        Validate.validState(!operations.isEmpty(), "cannot forward without an ongoing operation");
        final Operation<?> owner = operations.peek();
        Validate
            .isTrue(ObjectUtils.notEqual(owner, operation), "operations %s and %s are same/equal", owner, operation);
        final RESULT result = eval(operation);
        if (operation.isSuccessful()) {
            if (callback != null) {
                callback.run(result);
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
