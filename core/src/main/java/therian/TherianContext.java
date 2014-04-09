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
import org.apache.commons.functor.core.Constant;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static class OperationRequest {
        static class RecursionException extends Exception {
            private static final long serialVersionUID = 1L;
        }

        final Operation<?> operation;
        final Set<Hint> effectiveHints;

        OperationRequest(Operation<?> operation, Set<Hint> effectiveHints) {
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
            final OperationRequest other = (OperationRequest) obj;
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

    private static class Frame {
        /**
         * General-purpose root stack frame.
         */
        static final Frame ROOT = new Frame(null, null, Collections.<Class<? extends Hint>, Hint> emptyMap());

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

        final Frame parent;
        final Operation<?> operation;
        final Map<Class<? extends Hint>, Hint> hints;
        final OperationRequest key;

        private Frame(Frame parent, Operation<?> operation, Map<Class<? extends Hint>, Hint> hints) {
            this.parent = parent;
            this.operation = operation;
            this.hints = hints;
            this.key = toKey();
        }

        Frame(Frame parent, Operation<?> operation, Hint... hints) {
            this(parent, operation, toMap(hints));
        }

        private void populate(Map<Class<? extends Hint>, Hint> hintsMap) {
            if (!isRoot()) {
                parent.populate(hintsMap);
            }
            hintsMap.putAll(hints);
        }

        Set<Hint> effectiveHints() {
            final Map<Class<? extends Hint>, Hint> m = new HashMap<Class<? extends Hint>, TherianContext.Hint>();
            populate(m);
            return new LinkedHashSet<Hint>(m.values());
        }

        OperationRequest toKey() {
            return new OperationRequest(operation, effectiveHints());
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
        
        boolean find(OperationRequest key) {
            return this.key.equals(key) || !isRoot() && parent.find(key);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(TherianContext.class);
    private static final ThreadLocal<TherianContext> CURRENT_INSTANCE = new ThreadLocal<TherianContext>();

    private final Deque<Frame> evalStack = new ArrayDeque<Frame>();
    private final Deque<Frame> supportStack = new ArrayDeque<Frame>();
    private final Map<OperationRequest, Function<Operation<?>, ?>> cache = new HashMap<OperationRequest, Function<Operation<?>,?>>();

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
    public synchronized boolean supports(final Operation<?> operation, Hint... hints) {
        try {
            // TODO cache
            return handle(Constant.truePredicate(), supportStack, operation, hints);
        } catch (OperationRequest.RecursionException e) {
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

        final boolean dummyRoot = evalStack.isEmpty();
        if (dummyRoot) {
            // add a root frame to preserve our cache "around" the supports/eval lifecycle:
            push(Frame.ROOT, evalStack);
        }
        try {
            return supports(operation) ? eval(operation) : defaultValue;
        } finally {
            if (dummyRoot) {
                pop(Frame.ROOT, evalStack);
            }
        }
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
            // add a root frame to preserve our cache "around" the supports/eval lifecycle:
            push(Frame.ROOT, evalStack);
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
        try {
            handle(new Predicate<Operator<?>>() {

                @Override
                public boolean test(Operator<?> operator) {
                    if (evalRaw(operation, operator)) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Successfully evaluated {} with operator {}", operation, operator);
                        }
                        operation.setSuccessful(true);
                        return true;
                    }
                    return false;
                }
            }, evalStack, operation, hints);
        } catch (OperationRequest.RecursionException e) {
            throw new OperationException(operation, "recursive operation detected");
        }
        // TODO cache result
        return operation.getResult();
    }

    /**
     * Delegate the success of the current operation to that of another.
     * 
     * @param <RESULT>
     * @param operation
     * @param callback
     * @param hints
     * @return operation's success
     */
    public final synchronized <RESULT> boolean forwardTo(final Operation<RESULT> operation,
        final Callback<? super RESULT> callback, Hint... hints) {
        Validate.validState(!evalStack.isEmpty(), "cannot forward without an ongoing operation");

        final OperationRequest request = new Frame(evalStack.peek(), operation, hints).toKey();
        Validate.isTrue(ObjectUtils.notEqual(evalStack.peek().key, request), "Illegal attempt to forward to %s",
            request);

        final RESULT result = eval(operation, hints);
        if (operation.isSuccessful()) {
            if (callback != null) {
                callback.handle(result);
            }
            return true;
        }
        return false;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private boolean evalRaw(Operation operation, Operator operator) {
        return operator.perform(this, operation);
    }

    private synchronized <OPERATION extends Operation<?>> boolean handle(Predicate<? super Operator<?>> handler,
        Deque<Frame> stack, OPERATION operation, Hint... hints)
        throws OperationRequest.RecursionException {
        Validate.notNull(operation, "operation");
        Validate.noNullElements(hints, "null element at hints[%s]");

        final Frame top = stack.peek();
        final Frame f = new Frame(top, operation, hints);
        if (top != null && top.find(f.key)) {
            throw new OperationRequest.RecursionException();
        }

        push(f, stack);

        final TherianContext originalContext = getCurrentInstance();
        if (originalContext != this) {
            CURRENT_INSTANCE.set(this);
        }

        try {
            final Iterable<Operator<?>> supportingOperators = supportChecker.operatorsSupporting(operation);

            for (Operator<?> operator : supportingOperators) {
                if (handler.test(operator)) {
                    return true;
                }
            }
            return false;
        } finally {
            if (originalContext == null) {
                CURRENT_INSTANCE.remove();
            } else if (originalContext != this) {
                // restore original context in the unlikely event that multiple contexts are being used on the same
                // thread:
                CURRENT_INSTANCE.set(originalContext);
            }
            pop(f, stack);
        }
    }

    private synchronized void push(Frame frame, Deque<Frame> stack) {
        stack.push(frame);
        frame.join(this);
    }

    private synchronized void pop(Frame frame, Deque<Frame> stack) {
        final Frame popFrame = stack.pop();
        Validate.validState(popFrame == frame, "operation stack out of whack; found %s where %s was expected",
                popFrame.key, frame.key);
        frame.part(this);
        // if no ongoing evaluations or support checks, clear cache:
        if (evalStack.isEmpty() && supportStack.isEmpty()) {
            cache.clear();
        }
    }
}
