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
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.el.ELContext;
import javax.el.ELResolver;

import org.apache.commons.functor.Function;
import org.apache.commons.functor.Predicate;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import therian.Operator.Phase;
import therian.OperatorManager.SupportChecker;
import therian.el.TherianContextELResolver;
import uelbox.ELContextWrapper;

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
     * Generalizes a hint targeted to some {@link Operator} that can be set on the context. Note that a Hint should
     * properly implement {@link #equals(Object)} and {@link #hashCode()}.
     * 
     * @see TherianContext#doWithHints(Job, Hint...)
     */
    public interface Hint {

        /**
         * Get the hint type to use.
         * 
         * @return Class
         */
        Class<? extends Hint> getType();
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

    static class OperationRequest<RESULT> {
        final Operation<RESULT> operation;
        final Set<Hint> effectiveHints;

        OperationRequest(Operation<RESULT> operation, Set<Hint> effectiveHints) {
            super();
            this.operation = operation;
            this.effectiveHints = effectiveHints;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof OperationRequest)) {
                return false;
            }
            final OperationRequest<?> other = (OperationRequest<?>) obj;
            return new EqualsBuilder().append(operation, other.operation).append(effectiveHints, other.effectiveHints)
                .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(operation).append(effectiveHints).toHashCode();
        }

        @Override
        public String toString() {
            return String.format("%s: %s %s", OperationRequest.class.getSimpleName(), operation, effectiveHints);
        }
    }

    private static class Frame<RESULT> {
        static class RecursionException extends Exception {
            private static final long serialVersionUID = 1L;

            private final OperationRequest<?> duplicate;

            RecursionException(OperationRequest<?> duplicate) {
                this.duplicate = duplicate;
            }
        }

        /**
         * General-purpose root stack frame.
         */
        static final Frame<Void> ROOT = new Frame<Void>();

        static Map<Class<? extends Hint>, Hint> toMap(Hint[] hints) {
            final Map<Class<? extends Hint>, Hint> localHints = new LinkedHashMap<Class<? extends Hint>, Hint>();
            for (Hint hint : hints) {
                final Class<? extends Hint> key = hint.getType();
                if (localHints.containsKey(key)) {
                    throw new IllegalArgumentException(String.format("Found hints [%s, %s] with same type %s",
                        localHints.get(key), hint, key));
                }
                localHints.put(key, hint);
            }
            return Collections.unmodifiableMap(localHints);
        }

        final Operation<RESULT> operation;
        final Map<Class<? extends Hint>, Hint> hints;
        private Frame<?> parent;
        private OperationRequest<RESULT> key;

        private Frame() {
            this.operation = null;
            this.hints = Collections.emptyMap();
        }

        Frame(Operation<RESULT> operation, Hint... hints) {
            this.operation = Validate.notNull(operation, "operation");
            this.hints = toMap(Validate.noNullElements(hints, "null element at hints[%s]"));
        }

        boolean isRoot() {
            return parent == null;
        }

        Hint getHint(Class<? extends Hint> type) {
            if (hints.containsKey(type)) {
                return hints.get(type);
            }
            return isRoot() ? null : parent.getHint(type);
        }

        void join(TherianContext context) {
            for (Map.Entry<Class<? extends Hint>, Hint> e : hints.entrySet()) {
                context.putContext(e.getKey(), e.getValue());
            }
        }

        void part(TherianContext context) {
            for (Map.Entry<Class<? extends Hint>, Hint> e : hints.entrySet()) {
                context.putContext(e.getKey(), isRoot() ? null : parent.getHint(e.getKey()));
            }
        }

        <T> OperationRequest<T> find(OperationRequest<T> key) {
            @SuppressWarnings("unchecked")
            final OperationRequest<T> localKey = (OperationRequest<T>) getKey();
            if (localKey.equals(key)) {
                return localKey;
            }
            return isRoot() ? null : parent.find(key);
        }

        private void populate(Map<Class<? extends Hint>, Hint> hintsMap) {
            if (!isRoot()) {
                parent.populate(hintsMap);
            }
            hintsMap.putAll(hints);
        }

        private Set<Hint> effectiveHints() {
            final Map<Class<? extends Hint>, Hint> m = new HashMap<Class<? extends Hint>, Hint>();
            populate(m);
            return new LinkedHashSet<Hint>(m.values());
        }

        private synchronized OperationRequest<RESULT> getKey() {
            if (key == null) {
                key = new OperationRequest<RESULT>(operation, effectiveHints());
            }
            return key;
        }

        OperationRequest<RESULT> setParent(Frame<?> parent) throws RecursionException {
            this.parent = parent;
            key = null;
            final OperationRequest<RESULT> result = getKey();

            if (!isRoot()) {
                OperationRequest<RESULT> duplicateKey = parent.find(result);
                if (duplicateKey != null) {
                    throw new Frame.RecursionException(duplicateKey);
                }
            }
            return result;
        }

    }

    private interface CachedEvaluator<T> extends Predicate<Operation<T>>, Function<Operation<T>, T> {
    }

    private class CachedOperator<T> implements CachedEvaluator<T> {
        final Operator<? extends Operation<T>> operator;

        CachedOperator(Operator<? extends Operation<T>> operator) {
            super();
            this.operator = operator;
        }

        @Override
        public boolean test(Operation<T> operation) {
            if (evalRaw(operation, operator)) {
                operation.setSuccessful(true);
                return true;
            }
            return false;
        }

        @Override
        public T evaluate(Operation<T> operation) {
            if (test(operation)) {
                return operation.getResult();
            }
            throw new OperationException(operation);
        }

    }

    private static final Logger LOG = LoggerFactory.getLogger(TherianContext.class);
    private static final ThreadLocal<TherianContext> CURRENT_INSTANCE = new ThreadLocal<TherianContext>();

    private final Deque<Frame<?>> evalStack = new ArrayDeque<Frame<?>>();
    private final Deque<Frame<?>> supportStack = new ArrayDeque<Frame<?>>();
    private final Map<OperationRequest<?>, CachedEvaluator<?>> cache =
        new HashMap<OperationRequest<?>, CachedEvaluator<?>>();

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
     * Learn whether {@code operation} is supported by this context.
     * 
     * @param operation
     * @return boolean
     * @throws NullPointerException on {@code null} input
     */
    public synchronized <RESULT> boolean supports(final Operation<RESULT> operation, Hint... hints) {
        try {
            return handle(Operator.Phase.SUPPORT_CHECK, new Frame<RESULT>(operation, hints));
        } catch (Frame.RecursionException e) {
            return false;
        }
    }

    /**
     * Evaluates {@code operation} if supported; otherwise returns {@code null}. You may distinguish between a
     * {@code null} result and "not supported" by calling {@link #supports(Operation)} and {@link #eval(Operation)}
     * independently.
     * 
     * @param operation
     * @param hints
     * @return RESULT or {@code null}
     * @throws NullPointerException on {@code null} input
     * @throws OperationException potentially, via {@link Operation#getResult()}
     */
    public final synchronized <RESULT, OPERATION extends Operation<RESULT>> RESULT evalIfSupported(OPERATION operation,
        Hint... hints) {
        return evalIfSupported(operation, null, hints);
    }

    /**
     * Evaluates {@code operation} if supported; otherwise returns {@code defaultValue}.
     * 
     * @param operation
     * @param defaultValue
     * @param hints
     * @return RESULT or {@code defaultValue}
     * @throws NullPointerException on {@code null} input
     * @throws OperationException potentially, via {@link Operation#getResult()}
     */
    public final synchronized <RESULT, OPERATION extends Operation<RESULT>> RESULT evalIfSupported(OPERATION operation,
        RESULT defaultValue, Hint... hints) {
        return evalSuccess(operation, hints) ? operation.getResult() : defaultValue;
    }

    /**
     * Convenience method to perform an operation, discarding its result, and report whether it succeeded.
     * 
     * @param callback
     * @param operation
     * @param hints
     * @return whether {@code operation} was supported and successful
     * @throws NullPointerException on {@code null} input
     * @throws OperationException potentially, via {@link Operation#getResult()}
     */
    public final synchronized <RESULT> boolean evalSuccess(Callback<? super RESULT> callback, Operation<RESULT> operation,
        Hint... hints) {
        if (evalSuccess(operation, hints)) {
            if (callback != null) {
                callback.handle(operation.getResult());
            }
            return true;
        }
        return false;
    }

    /**
     * Convenience method to perform an operation, discarding its result, and report whether it succeeded.
     * 
     * @param operation
     * @param hints
     * @return whether {@code operation} was supported and successful
     * @throws NullPointerException on {@code null} input
     * @throws OperationException potentially, via {@link Operation#getResult()}
     */
    public final synchronized boolean evalSuccess(Operation<?> operation, Hint... hints) {
        final boolean dummyRoot = evalStack.isEmpty();
        if (dummyRoot) {
            // add a root frame to preserve our cache "around" the supports/eval lifecycle, bypassing #push():
            evalStack.push(Frame.ROOT);
        }
        try {
            if (supports(operation, hints)) {
                eval(operation, hints);
                return operation.isSuccessful();
            }
        } finally {
            if (dummyRoot) {
                pop(Frame.ROOT, evalStack);
            }
        }
        return false;
    }

    /**
     * Performs the specified {@link Operation} by invoking any compatible {@link Operator} until the {@link Operation}
     * is marked as having been successful, then returns the result from {@link Operation#getResult()}. Note that
     * <em>most</em> unsuccessful {@link Operation}s will, at this point, throw an {@link OperationException}.
     * 
     * @param RESULT
     * @param OPERATION
     * @param operation
     * @param hints added as context objects which are accessible to any {@link Operator}s that are aware of them;
     *            should be unique by {@link Hint#getType()}
     * @return RESULT
     * @throws NullPointerException on {@code null} input
     * @throws OperationException potentially, via {@link Operation#getResult()}
     * @see #eval(Operation)
     */
    public final synchronized <RESULT, OPERATION extends Operation<RESULT>> RESULT eval(final OPERATION operation,
        Hint... hints) {
        return eval(new Frame<RESULT>(operation, hints));
    }

    private synchronized <RESULT> RESULT eval(final Frame<RESULT> frame) {
        try {
            handle(Operator.Phase.EVALUATION, frame);
        } catch (Frame.RecursionException e) {
            if (e.duplicate.operation.isSuccessful()) {
                @SuppressWarnings("unchecked")
                final RESULT result = (RESULT) e.duplicate.operation.getResult();
                frame.operation.setSuccessful(true);
                return result;
            }
            throw new OperationException(frame.operation, "recursive operation detected");
        }
        return (RESULT) frame.operation.getResult();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private boolean evalRaw(Operation operation, Operator operator) {
        return operator.perform(this, operation);
    }

    private synchronized <RESULT> boolean handle(Phase phase, Frame<RESULT> frame) throws Frame.RecursionException {

        final Deque<Frame<?>> stack;
        switch (phase) {
        case SUPPORT_CHECK:
            stack = supportStack;
            break;

        case EVALUATION:
            stack = evalStack;
            break;

        default:
            throw new IllegalArgumentException("Unknown phase");
        }

        final OperationRequest<?> request = push(frame, stack);

        final TherianContext originalContext = getCurrentInstance();
        if (originalContext != this) {
            CURRENT_INSTANCE.set(this);
        }

        try {
            @SuppressWarnings("rawtypes")
            final CachedEvaluator cachedEvaluator = cache.get(request);

            if (cachedEvaluator != null) {
                switch (phase) {
                case SUPPORT_CHECK:
                    return true;

                case EVALUATION:
                    @SuppressWarnings("unchecked")
                    boolean eval = cachedEvaluator.test(frame.operation);
                    if (eval) {
                        return true;
                    }
                    break;

                default:
                    break;
                }
            }

            final Iterable<Operator<?>> supportingOperators = supportChecker.operatorsSupporting(frame.operation);

            for (final Operator<?> operator : supportingOperators) {
                boolean success = false;
                switch (phase) {
                case SUPPORT_CHECK:
                    success = true;
                    break;

                case EVALUATION:
                    if (evalRaw(frame.operation, operator)) {
                        LOG.debug("Successfully evaluated {} with operator {}", frame.operation, operator);
                        success = true;
                        break;
                    }

                default:
                    break;
                }
                if (success) {
                    @SuppressWarnings("unchecked")
                    // supports; therefore safe:
                    final Operator<? extends Operation<RESULT>> strongOperator =
                        (Operator<? extends Operation<RESULT>>) operator;
                    cache.put(frame.key, new CachedOperator<RESULT>(strongOperator));
                    frame.operation.setSuccessful(true);
                    return true;
                }
            }
            return false;
        } finally {
            if (originalContext == null) {
                CURRENT_INSTANCE.remove();
            } else if (originalContext != this) {
                // restore original context
                CURRENT_INSTANCE.set(originalContext);
            }
            pop(frame, stack);
        }
    }

    private synchronized OperationRequest<?> push(Frame<?> frame, Deque<Frame<?>> stack)
        throws Frame.RecursionException {
        final OperationRequest<?> result = frame.setParent(stack.peek());
        stack.push(frame);
        frame.join(this);
        return result;
    }

    private synchronized void pop(Frame<?> frame, Deque<Frame<?>> stack) {
        final Frame<?> popFrame = stack.pop();
        Validate.validState(popFrame == frame, "operation stack out of whack; found %s where %s was expected",
            popFrame.key, frame.key);
        frame.part(this);

        // if no ongoing evaluations or support checks, clear cache:
        if (evalStack.isEmpty() && supportStack.isEmpty()) {
            cache.clear();
        }
    }
}
