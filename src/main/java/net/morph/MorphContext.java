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
package net.morph;

import java.util.ArrayDeque;
import java.util.Deque;

import javax.el.ELContext;
import javax.el.ELResolver;

import net.morph.el.MorphContextELResolver;
import net.morph.util.ReadOnlyUtils;

import org.apache.commons.functor.core.collection.FilteredIterable;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;

import uelbox.ELContextWrapper;

/**
 * Morph context.
 */
public class MorphContext extends ELContextWrapper {
    /**
     * Nested {@link ELContextWrapper} that wraps what this {@link MorphContext}
     * wraps, which can be used with "utility" {@link ELResolver} wrappers.
     */
    public abstract class NestedELContextWrapper extends ELContextWrapper {

        public NestedELContextWrapper() {
            super(MorphContext.this.wrapped);
        }
    }

    private static final ThreadLocal<MorphContext> CURRENT_INSTANCE = new ThreadLocal<MorphContext>();

    private Deque<Operation<?>> operations = new ArrayDeque<Operation<?>>();

    MorphContext(ELContext wrapped) {
        super(wrapped);
    }

    @Override
    protected ELResolver wrap(ELResolver elResolver) {
        return new MorphContextELResolver(elResolver);
    }

    public static MorphContext getCurrentInstance() {
        return CURRENT_INSTANCE.get();
    }

    public static MorphContext getRequiredInstance() {
        final MorphContext result = getCurrentInstance();
        Validate.validState(result != null);
        return result;
    }

    /**
     * Performs the specified operation by invoking any compatible operator
     * until the operation is marked as successful, then returns the result.
     * 
     * @param operation
     * @param <RESULT>
     * @param <OPERATION>
     * @return result if available
     * @throws IllegalArgumentException
     *             if no operators are successful
     */
    public final synchronized <RESULT, OPERATION extends Operation<RESULT>> RESULT eval(OPERATION operation) {
        final MorphContext originalContext = getCurrentInstance();
        if (originalContext != this) {
            CURRENT_INSTANCE.set(this);
        }
        try {
            if (operations.contains(operation)) {
                //TODO handle better, e.g. copy of recursive graph
                throw new OperationException(operation, "recursive operation detected");
            }
            operations.push(operation);

            try {
                operation.init();
                if (!operation.isSuccessful()) {
                    for (Operator<?> operator : FilteredIterable.of(getTypedContext(Morph.class).getOperators())
                        .retain(Operators.supporting(operation))) {
                        // already determined that operator supports operation:
                        evalRaw(operation, operator);
                        if (operation.isSuccessful()) {
                            break;
                        }
                    }
                }
                return operation.getResult();
            } finally {
                assert operations.pop() == operation;
            }
        } finally {
            // javadoc not clear on whether set(null) is truly equivalent to
            // remove():
            if (originalContext == null) {
                CURRENT_INSTANCE.remove();
            } else if (originalContext != this) {
                // restore original context in the unlikely event that multiple
                // contexts are being used on the same
                // thread:
                CURRENT_INSTANCE.set(originalContext);
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void evalRaw(Operation operation, Operator operator) {
        Operators.validateImplementation(operator).perform(operation);
    }

    /**
     * Delegate the success of the current operation to that of another.
     * 
     * @param operation
     * @param <RESULT>
     * @param <OPERATION>
     * @return operation's result
     */
    public final synchronized <RESULT, OPERATION extends Operation<RESULT>> RESULT forwardTo(OPERATION operation) {
        Validate.validState(!operations.isEmpty(), "cannot delegate without an ongoing operation");
        RESULT result;
        Operation<?> owner = operations.peek();
        try {
            Validate.isTrue(ObjectUtils.notEqual(owner, operation), "operations %s and %s are same/equal", owner,
                operation);
            result = eval(operation);
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
