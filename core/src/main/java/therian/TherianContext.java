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
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.el.ELContext;
import javax.el.ELResolver;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;

import therian.Operator.Phase;
import therian.OperatorManager.SupportChecker;
import therian.behavior.Caching;
import therian.el.TherianContextELResolver;
import uelbox.ELContextWrapper;

/**
 * Therian context.
 */
public class TherianContext extends ELContextWrapper {

    /**
     * Represents a requested {@link Operation}.
     *
     * @param <RESULT>
     */
    public static class OperationRequest<RESULT> {
        /**
         * Requested {@link Operation}.
         */
        public final Operation<RESULT> operation;

        /**
         * Effective {@link Hint}s.
         */
        public final Set<Hint> effectiveHints;

        /**
         * Evaluation {@link Phase}.
         */
        public final Operator.Phase phase;

        private final String format;

        private OperationRequest(Operation<RESULT> operation, Set<Hint> effectiveHints, Phase phase) {
            super();
            this.operation = operation;
            this.effectiveHints = effectiveHints;
            this.phase = phase;
            this.format = effectiveHints.isEmpty() ? "%s: %s %s" : "%s: %s %s %s";
        }

        /*
         * Note that equals/hashCode include only operation and hints.
         */
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

        /*
         * Note that equals/hashCode include only operation and hints.
         */
        @Override
        public int hashCode() {
            return Objects.hash(operation, effectiveHints);
        }

        @Override
        public String toString() {
            return String.format(format, OperationRequest.class.getSimpleName(), operation, phase, effectiveHints);
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
        static final Frame<Void> ROOT = new Frame<>();

        private static final String BRANCH = "\u2514\u2500";

        static Map<Class<? extends Hint>, Hint> toMap(Hint[] hints) {
            if (hints.length == 0) {
                return Collections.emptyMap();
            }
            final Map<Class<? extends Hint>, Hint> localHints = new LinkedHashMap<>();
            for (Hint hint : hints) {
                final Class<? extends Hint> key = hint.getType();
                Validate.isTrue(!localHints.containsKey(key), "Found hints [%s, %s] with same type %s",
                    localHints.get(key), hint, key);

                localHints.put(key, hint);
            }
            return Collections.unmodifiableMap(localHints);
        }

        final Phase phase;
        final Operation<RESULT> operation;
        final Map<Class<? extends Hint>, Hint> hints;
        private Frame<?> parent;
        private OperationRequest<RESULT> key;
        private String lead;

        private Frame() {
            this.phase = Phase.EVALUATION;
            this.operation = null;
            this.hints = Collections.emptyMap();
        }

        Frame(Phase phase, Operation<RESULT> operation, Hint... hints) {
            this.phase = Validate.notNull(phase, "phase");
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
                final Class<? extends Hint> hintKey = e.getKey();
                final Hint previous = isRoot() ? null : parent.getHint(hintKey);
                if (previous == null) {
                    context.removeContext(hintKey);
                } else {
                    context.putContext(hintKey, previous);
                }
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
            final Map<Class<? extends Hint>, Hint> m = new HashMap<>();
            populate(m);
            if (m.isEmpty()) {
                return Collections.emptySet();
            }
            return Collections.unmodifiableSet(new LinkedHashSet<>(m.values()));
        }

        private synchronized OperationRequest<RESULT> getKey() {
            if (key == null) {
                key = new OperationRequest<>(operation, effectiveHints(), phase);
            }
            return key;
        }

        OperationRequest<RESULT> setParent(Frame<?> parent) throws Frame.RecursionException {
            this.parent = parent;
            key = null;
            final OperationRequest<RESULT> result = getKey();

            if (isRoot()) {
                lead = "";
            } else {
                final OperationRequest<RESULT> duplicateKey = parent.find(result);
                if (duplicateKey != null) {
                    throw new Frame.RecursionException(duplicateKey);
                }
                lead = StringUtils.repeat(' ', (depth(parent) - 1) * 2) + BRANCH;
            }
            return result;
        }

        int depth(Frame<?> parent) {
            int result = 0;
            for (Frame<?> f = parent; f != null && f.key != null; f = f.parent, result++);
            return result;
        }

        String logString() {
            return lead + getKey();
        }
    }

    private interface CachedEvaluator<T> {
        boolean evaluate(Operation<T> operation);
    }

    private static final ThreadLocal<TherianContext> CURRENT_INSTANCE = new ThreadLocal<>();

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
     * Nested {@link ELContextWrapper} that wraps what this {@link TherianContext} wraps, which can be used with
     * "utility" {@link ELResolver} wrappers.
     */
    public abstract class NestedELContextWrapper extends ELContextWrapper {

        public NestedELContextWrapper() {
            super(TherianContext.this.wrapped);
        }
    }

    private class CachedOperator<T> implements CachedEvaluator<T> {
        final Operator<? extends Operation<T>> operator;

        CachedOperator(Operator<? extends Operation<T>> operator) {
            super();
            this.operator = operator;
        }

        @Override
        public boolean evaluate(Operation<T> operation) {
            try {
                return evalRaw(operation, operator);
            } catch (Exception e) {
                return false;
            }
        }
    }

    private class CachedResult<T> implements CachedEvaluator<T> {
        final T value;

        CachedResult(T value) {
            super();
            this.value = value;
        }

        @Override
        public boolean evaluate(Operation<T> operation) {
            operation.setResult(value);
            return true;
        }
    }

    private final Deque<Frame<?>> stack = new ArrayDeque<>();
    private final Map<OperationRequest<?>, CachedEvaluator<?>> cache = new HashMap<>();

    private final SupportChecker supportChecker;
    private final Therian parent;
    private final Logger logger;

    TherianContext(ELContext wrapped, Therian parent) {
        super(wrapped);
        this.parent = Validate.notNull(parent, "parent");
        supportChecker = parent.getOperatorManager().new SupportChecker(this);
        logger = parent.getLogger(getClass());
    }

    @Override
    protected ELResolver wrap(ELResolver elResolver) {
        return new TherianContextELResolver(elResolver);
    }

    /**
     * Get a view of the {@link Operation}s currently being evaluated. In the manner of a stack, the first element is
     * the nearest and the last element is the farthest.
     * 
     * @return {@link Stream} of {@link OperationRequest}
     */
    public Stream<OperationRequest<?>> getRequestStack() {
        return stack.stream().map(Frame::getKey);
    }

    /**
     * Learn whether {@code operation} is supported by this context.
     *
     * @param operation
     * @return boolean
     * @throws NullPointerException on {@code null} input
     */
    public synchronized <RESULT> boolean supports(final Operation<RESULT> operation, Hint... hints) {
        final Frame<RESULT> frame = new Frame<>(Phase.SUPPORT_CHECK, operation, hints);
        try {
            return handle(frame);
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
    public final synchronized <RESULT> boolean evalSuccess(Consumer<? super RESULT> callback,
        Operation<RESULT> operation, Hint... hints) {
        if (evalSuccess(operation, hints)) {
            if (callback != null) {
                callback.accept(operation.getResult());
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
        final boolean dummyRoot = stack.isEmpty();
        if (dummyRoot) {
            // add a root frame to preserve our cache "around" the supports/eval lifecycle, bypassing #push():
            stack.push(Frame.ROOT);
        }
        try {
            if (supports(operation, hints)) {
                eval(operation, hints);
                return operation.isSuccessful();
            }
        } finally {
            if (dummyRoot) {
                pop(Frame.ROOT);
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
        final Frame<RESULT> frame = new Frame<>(Phase.EVALUATION, operation, hints);
        try {
            handle(frame);
        } catch (Frame.RecursionException e) {
            if (e.duplicate.operation.isSuccessful()) {
                @SuppressWarnings("unchecked")
                final RESULT result = (RESULT) e.duplicate.operation.getResult();
                operation.setSuccessful(true);
                operation.setResult(result);
                return result;
            }
            throw new OperationException(frame.operation, "recursive operation detected");
        }
        return operation.getResult();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private boolean evalRaw(Operation operation, Operator operator) {
        return operator.perform(this, operation);
    }

    private synchronized <RESULT> boolean handle(Frame<RESULT> frame) throws Frame.RecursionException {
        final OperationRequest<?> request = push(frame);

        if (logger.isTraceEnabled()) {
            logger.trace("{} requested", frame.logString());
        }
        final TherianContext originalContext = getCurrentInstance();
        if (originalContext != this) {
            CURRENT_INSTANCE.set(this);
        }
        final Caching caching = parent.getBehavior(Caching.class, Caching.ALL);

        try {
            if (caching.implies(Caching.CONTEXT)) {
                @SuppressWarnings("rawtypes")
                final CachedEvaluator cachedEvaluator = cache.get(request);

                if (cachedEvaluator != null) {
                    switch (frame.phase) {
                    case SUPPORT_CHECK:
                        return true;

                    case EVALUATION:
                        @SuppressWarnings("unchecked")
                        boolean eval = cachedEvaluator.evaluate(frame.operation);
                        if (eval) {
                            frame.operation.setSuccessful(true);
                            return true;
                        }
                        break;

                    default:
                        break;
                    }
                }
            }
            final Iterable<Operator<?>> supportingOperators = supportChecker.operatorsSupporting(frame.operation);

            final Operator.Phase phase = frame.phase;
            for (final Operator<?> operator : supportingOperators) {
                if (phase == Phase.SUPPORT_CHECK || phase == Phase.EVALUATION && evalRaw(frame.operation, operator)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("{} handled by operator {}", frame.logString(), operator);
                    }
                    if (phase == Phase.EVALUATION) {
                        frame.operation.setSuccessful(true);
                    }
                    if (caching.implies(Caching.CONTEXT) && Caching.isReusable(operator, phase)) {

                        switch (phase) {
                        case SUPPORT_CHECK:
                            if (!cache.containsKey(request)) {
                                @SuppressWarnings("unchecked")
                                // supports; therefore safe:
                                final Operator<? extends Operation<RESULT>> strongOperator =
                                    (Operator<? extends Operation<RESULT>>) operator;
                                cache.put(request, new CachedOperator<>(strongOperator));
                            }
                            break;
                        case EVALUATION:
                            if (!(cache.get(request) instanceof CachedResult<?>)) {
                                cache.put(request, new CachedResult<>(frame.operation.getResult()));
                            }
                            break;
                        default:
                            break;
                        }
                    }
                    supportChecker.record(frame.operation, operator);
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
            pop(frame);
        }
    }

    private synchronized OperationRequest<?> push(Frame<?> frame) throws Frame.RecursionException {
        final OperationRequest<?> result = frame.setParent(stack.peek());
        stack.push(frame);
        frame.join(this);
        return result;
    }

    private synchronized void pop(Frame<?> frame) {
        final Frame<?> popFrame = stack.pop();
        Validate.validState(popFrame == frame, "operation stack out of whack; found %s where %s was expected",
            popFrame.getKey(), frame.getKey());

        frame.part(this);

        // clear cache when stack is empty:
        if (stack.isEmpty()) {
            cache.clear();
        }
    }
}
