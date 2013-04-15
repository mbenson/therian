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

import org.apache.commons.functor.UnaryProcedure;
import org.apache.commons.functor.core.NoOp;
import org.apache.commons.functor.core.collection.FilteredIterable;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;

import therian.el.TherianContextELResolver;
import therian.uelbox.ELContextWrapper;
import therian.util.ReadOnlyUtils;

/**
 * Therian context.
 */
public class TherianContext extends ELContextWrapper {
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
    public static TherianContext getCurrentInstance() {
        return CURRENT_INSTANCE.get();
    }

    /**
     * Require current thread-bound instance.
     * 
     * @return {@link TherianContext}
     * @throws IllegalStateException if unavailable
     */
    public static TherianContext getRequiredInstance() {
        final TherianContext result = getCurrentInstance();
        Validate.validState(result != null);
        return result;
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

    public boolean supports(Operation<?> operation) {
        final TherianContext originalContext = getCurrentInstance();
        if (originalContext != this) {
            CURRENT_INSTANCE.set(this);
        }
        try {
            return FilteredIterable.of(getTypedContext(Therian.class).getOperators())
                .retain(Operators.supporting(operation)).iterator().hasNext();
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
     * Performs the specified {@link Operation} by invoking any compatible {@link Operator} until the {@link Operation}
     * is marked as having been successful, then returns the result from {@link Operation#getResult()}. Note that
     * <em>most</em> unsuccessful {@link Operation}s will, at this point, throw an {@link OperationException}.
     * 
     * @param operation
     * @param <RESULT>
     * @param <OPERATION>
     * @return result if available
     * @throws OperationException potentially, via {@link Operation#getResult()}
     */
    public final synchronized <RESULT, OPERATION extends Operation<RESULT>> RESULT eval(OPERATION operation) {
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

            try {
                for (Operator<?> operator : FilteredIterable.of(getTypedContext(Therian.class).getOperators()).retain(
                    Operators.supporting(operation))) {
                    // already determined that operator supports operation:
                    evalRaw(operation, operator);
                    if (operation.isSuccessful()) {
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
    private void evalRaw(Operation operation, Operator operator) {
        operator.perform(operation);
    }

    /**
     * Delegate the success of the current operation to that of another.
     * 
     * @param operation
     * @param <RESULT>
     * @param <OPERATION>
     * @return operation's result
     */
    public final synchronized <RESULT, OPERATION extends Operation<RESULT>> RESULT forwardTo(final OPERATION operation) {
        return forwardTo(operation, NoOp.<RESULT> unaryInstance());
    }

    /**
     * Delegate the success of the current operation to that of another.
     * 
     * @param operation
     * @param callback for result
     * @param <RESULT>
     * @param <OPERATION>
     * @return operation's result
     */
    public final synchronized <RESULT, OPERATION extends Operation<RESULT>> RESULT forwardTo(final OPERATION operation,
        final UnaryProcedure<RESULT> callback) {
        Validate.validState(!operations.isEmpty(), "cannot delegate without an ongoing operation");
        RESULT result;
        Operation<?> owner = operations.peek();
        try {
            if (operation.isSuccessful()) {
                result = operation.getResult();
            } else {
                Validate.isTrue(ObjectUtils.notEqual(owner, operation), "operations %s and %s are same/equal", owner,
                    operation);
                result = eval(operation);
                callback.run(result);
            }
        } finally {
            owner.setSuccessful(operation.isSuccessful());
        }
        return result;
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
