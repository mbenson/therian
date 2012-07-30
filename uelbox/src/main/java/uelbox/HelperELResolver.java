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

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 * Abstract "helper" ELResolver: handles each step in the resolution of an expression, turning the ultimate set/invoke
 * operation into a no-op, and providing a result object after each such call. Automatically skips intervening/nested
 * calls between nodes of the "main" expression. Not thread-safe.
 */
public abstract class HelperELResolver<RESULT> extends ELResolverWrapper {
    /**
     * HelperELResolver.WithWorkingStorage
     * 
     * @param <WORKING_STORAGE>
     * @param <RESULT>
     */
    public abstract static class WithWorkingStorage<WORKING_STORAGE, RESULT> extends HelperELResolver<RESULT> {
        private class StateWithWorkingStorage extends WithWorkingStorage.State {
            WORKING_STORAGE workingStorage;

            @Override
            void reset() {
                super.reset();
                workingStorage = null;
            }
        }

        /**
         * HelperELResolver.WithWorkingStorage.AsResult
         * 
         * @param <WORKING_STORAGE>
         */
        public abstract static class AsResult<WORKING_STORAGE> extends
            WithWorkingStorage<WORKING_STORAGE, WORKING_STORAGE> {
            /**
             * Create a new HelperELResolver.WithWorkingStorage.AsResult.
             * 
             * @param wrapped
             */
            protected AsResult(ELResolver wrapped) {
                super(wrapped);
            }

        }

        /**
         * Create a new HelperELResolver.WithWorkingStorage.
         * 
         * @param wrapped
         */
        protected WithWorkingStorage(ELResolver wrapped) {
            super(wrapped);
        }

        /**
         * Get the working storage object in play for the current resolution in this context.
         * 
         * @param context
         * @return WORKING_STORAGE
         */
        protected final WORKING_STORAGE getWorkingStorage(ELContext context) {
            @SuppressWarnings("unchecked")
            final WORKING_STORAGE result =
                ((StateWithWorkingStorage) UEL.getContext(context, State.class)).workingStorage;
            return result;
        }

        @Override
        StateWithWorkingStorage getOrCreateState(ELContext context, Object base) {
            @SuppressWarnings("unchecked")
            final StateWithWorkingStorage state = (StateWithWorkingStorage) super.getOrCreateState(context, base);
            if (state.workingStorage == null) {
                state.workingStorage = createWorkingStorage(context, base);
            }
            return state;
        }

        @Override
        StateWithWorkingStorage createState(ELContext context, Object base) {
            return new StateWithWorkingStorage();
        }

        /**
         * Create a working storage object for the specified context and base object.
         * 
         * @param context
         * @param base
         * @return WORKING_STORAGE
         */
        protected abstract WORKING_STORAGE createWorkingStorage(ELContext context, Object base);

        @Override
        protected final void afterGetValue(ELContext context, Object base, Object property, Object value) {
            afterGetValue(context, base, property, value, getWorkingStorage(context));
        }

        protected abstract void afterGetValue(ELContext context, Object base, Object property, Object value,
            WORKING_STORAGE workingStorage);

        @Override
        protected final RESULT afterSetValue(ELContext context, Object base, Object property) {
            return afterSetValue(context, base, property, getWorkingStorage(context));
        }

        protected abstract RESULT afterSetValue(ELContext context, Object base, Object property,
            WORKING_STORAGE workingStorage);

    }

    enum Completion {
        NO, YES;
    }

    class State {
        final MutableInt depth = new MutableInt();
        Object tip;
        Completion completion;
        RESULT result;

        State() {
            reset();
        }

        void reset() {
            depth.setValue(0);
            tip = null;
            completion = Completion.NO;
        }
    }

    /**
     * Create a new HelperELResolver.
     * 
     * @param wrapped
     */
    protected HelperELResolver(ELResolver wrapped) {
        super(wrapped);
    }

    @Override
    public final Object getValue(ELContext context, Object base, Object property) {
        State state = getOrCreateState(context, base);
        Object value = super.getValue(context, base, property);

        // deal with the (unusual) case that we never receive a read against a
        // null base:
        if (state.tip == null && base != null) {
            state.tip = base;
        }

        // look for nested reads against tip:
        // e.g. base.aList[base.aList.size() - 1]
        if (value == state.tip) {
            if (base == value) {
                // somewhat odd, but value seems to be its own property, so
                // simply skip this node:
            } else {
                // record that we expect to see a property resolved against tip
                // before we pick up recording again:
                state.depth.increment();
            }
        } else if (base == state.tip) {
            // if applicable, pop out of the most recent nested read against
            // tip:
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

    @Override
    public final void setValue(ELContext context, Object base, Object property, Object value) {
        State state = getOrCreateState(context, base);
        Validate.validState(base == state.tip && state.depth.intValue() == 0);
        state.result = afterSetValue(context, base, property);
        state.completion = Completion.YES;
        context.setPropertyResolved(true);
    }

    /**
     * Post-process {@link #getValue(javax.el.ELContext, Object, Object)}. Default no-op.
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
     * @throws UnsupportedOperationException
     *             by default
     */
    protected RESULT afterSetValue(ELContext context, Object base, Object property) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get result. Should be called exactly once after a context operation (optional if the HelperELResolver instance is
     * not to be reused).
     * 
     * @return RESULT
     */
    public final RESULT getResult(ELContext context) {
        @SuppressWarnings("unchecked")
        final State state = UEL.getContext(context, State.class);
        Validate.validState(state != null && state.completion != Completion.NO);
        try {
            return state.result;
        } finally {
            state.reset();
        }
    }

    State getOrCreateState(ELContext context, Object base) {
        synchronized (context) {
            @SuppressWarnings("unchecked")
            State state = UEL.getContext(context, State.class);
            if (state == null) {
                state = createState(context, base);
                context.putContext(State.class, state);
            } else if (state.completion == Completion.YES) {
                state.reset();
            }
            state.tip = base;
            return state;
        }
    }

    State createState(ELContext context, Object base) {
        return new State();
    }
}
