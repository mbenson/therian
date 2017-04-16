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
package therian.position.relative;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.PropertyNotFoundException;
import javax.el.ValueExpression;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import therian.TherianContext;
import therian.position.Position;
import therian.util.Positions;
import therian.util.Types;
import uelbox.ELContextWrapper;
import uelbox.ELResolverWrapper;
import uelbox.HelperELContext;
import uelbox.HelperELResolver;
import uelbox.UEL;

/**
 * Fluent entry point for "expression at" {@link RelativePositionFactory}.
 */
public class Expression {

    /**
     * Expression {@link RelativePositionFactory}.
     *
     * @param <TYPE>
     */
    public static class PositionFactory<TYPE> extends RelativePositionFactory.ReadWrite<Object, TYPE> {

        private static class BaseContext extends ELContextWrapper {

            private BaseContext(final ELContext wrapped, final Object base) {
                super(wrapped);
                getVariableMapper().setVariable(
                    BASE_VARIABLE_NAME,
                    UEL.getExpressionFactory(this).createValueExpression(base,
                        base == null ? Object.class : base.getClass()));
            }

            @Override
            protected ELResolver wrap(ELResolver elResolver) {
                return elResolver;
            }
        }

        private static class WorkingStorage {
            Position.Readable<?> current;

            WorkingStorage(Position.Readable<?> initial) {
                current = initial;
            }

            void record(ELContext context, Object base, Object property, Object value) {
                final Type baseType = current.getType();

                if (TypeUtils.isArrayType(baseType)) {
                    current =
                        Element.atArrayIndex(UEL.coerceToType(context, Integer.class, property).intValue()).of(current);
                } else if (TypeUtils.isAssignable(baseType, Iterable.class)) {
                    @SuppressWarnings("unchecked")
                    final Position.Readable<Iterable<?>> iterablePosition = (Position.Readable<Iterable<?>>) current;
                    current =
                        Element.atIndex(UEL.coerceToType(context, Integer.class, property).intValue()).of(
                            iterablePosition);
                } else if (TypeUtils.isAssignable(baseType, Map.class)) {
                    @SuppressWarnings("unchecked")
                    final Position.Readable<Map<Object, Object>> mapPosition =
                        (Position.Readable<Map<Object, Object>>) current;
                    current = Keyed.value().at(property).of(mapPosition);
                } else {
                    current = Property.at(UEL.coerceToType(context, String.class, property)).of(current);
                    try {
                        current.getType();
                    } catch (IllegalStateException e) {
                        current = Positions.readOnly(value == null ? Object.class : value.getClass(), value);
                    }
                }
            }
        }

        private static final String BASE_VARIABLE_NAME = String.format("%s_baseVariable",
            Expression.PositionFactory.class.getName().replace('.', '_'));

        private final String expr;
        private final boolean optional;

        private PositionFactory(final String expr) {
            this(expr, false);
        }

        private PositionFactory(final String expr, boolean optional) {
            this.expr = UEL.strip(expr);
            this.optional = optional;
        }

        public String getExpression() {
            return expr;
        }

        @Override
        public <P> RelativePosition.ReadWrite<P, TYPE> of(Position.Readable<P> parentPosition) {
            class Result extends RelativePositionImpl<P, String> implements RelativePosition.ReadWrite<P, TYPE> {
                // cache most recent type calculation by context:
                private Pair<TherianContext, Type> cachedType;

                protected Result(Position.Readable<P> parentPosition, String expr) {
                    super(parentPosition, expr);
                }

                @Override
                public Type getType() {
                    return Types.refine(getExpressionType(), parentPosition.getType());
                }

                private Type getExpressionType() {
                    final TherianContext context = TherianContext.getInstance();
                    Type result = null;
                    synchronized (this) {
                        if (cachedType != null) {
                            if (cachedType.getLeft() == context) {
                                result = cachedType.getRight();
                            }
                        }
                    }

                    if (result == null) {
                        final BaseContext baseContext = new BaseContext(context, parentPosition.getValue());
                        final ValueExpression valueExpression = getExpression(baseContext);
                        final HelperELContext<Type> helperELContext = new HelperELContext<Type>(baseContext) {

                            @Override
                            protected HelperELResolver<Type> wrap(ELResolver elResolver) {
                                return new HelperELResolver.WithWorkingStorage<WorkingStorage, Type>(elResolver) {

                                    @Override
                                    protected WorkingStorage createWorkingStorage(ELContext context, Object base) {
                                        return new WorkingStorage(parentPosition);
                                    }

                                    @Override
                                    protected void afterGetValue(ELContext context, Object base, Object property,
                                        Object value, WorkingStorage workingStorage) {
                                        workingStorage.record(context, base, property, value);
                                    }

                                    @Override
                                    protected Type afterSetValue(ELContext context, Object base, Object property,
                                        WorkingStorage workingStorage) {
                                        workingStorage.record(context, base, property, null);
                                        return workingStorage.current.getType();
                                    }
                                };
                            }
                        };

                        try {
                            result = helperELContext.evaluate(valueExpression);
                        } catch (Exception e) {
                        }
                        if (result == null) {
                            try {
                                result = valueExpression.getType(baseContext);
                            } catch (Exception e) {
                            }
                        }
                        if (result == null && optional) {
                            final WorkingStorage workingStorage = new WorkingStorage(parentPosition);

                            // will only work with simple expressions:
                            final ELContextWrapper bruteForceContext = new ELContextWrapper(baseContext) {

                                @Override
                                protected ELResolver wrap(ELResolver elResolver) {
                                    return new ELResolverWrapper(UEL.nopELResolver()) {
                                        @Override
                                        public Object getValue(ELContext context, Object base, Object property) {
                                            workingStorage.record(context, base, property, null);
                                            context.setPropertyResolved(true);
                                            try {
                                                return workingStorage.current.getValue();
                                            } catch (Exception e) {
                                                return null;
                                            }
                                        }

                                        @Override
                                        public void setValue(ELContext context, Object base, Object property,
                                            Object value) {
                                            workingStorage.record(context, base, property, value);
                                            context.setPropertyResolved(true);
                                        }
                                    };
                                }
                            };
                            try {
                                valueExpression.setValue(bruteForceContext, null);
                            } catch (Exception e) {
                            }
                            if (bruteForceContext.isPropertyResolved()) {
                                result = workingStorage.current.getType();
                            }
                        }
                    }
                    return handle(context, result);
                }

                /**
                 * Handle result.
                 *
                 * @param context
                 * @param result
                 * @return result
                 * @throws IllegalStateException if {@code result == null}
                 */
                private Type handle(TherianContext context, Type result) {
                    synchronized (this) {
                        cachedType = ImmutablePair.of(context, result);
                    }
                    Validate.validState(result != null, "Cannot get type for expression " + expr);
                    return result;
                }

                @Override
                public TYPE getValue() {
                    final TherianContext context = TherianContext.getInstance();
                    final ValueExpression valueExpression =
                        getExpression(new BaseContext(context, parentPosition.getValue()));
                    try {
                        @SuppressWarnings("unchecked")
                        final TYPE result = (TYPE) valueExpression.getValue(context);
                        return result;
                    } catch (PropertyNotFoundException e) {
                        if (optional) {
                            return null;
                        }
                        throw e;
                    }
                }

                @Override
                public void setValue(TYPE value) {
                    final TherianContext context = TherianContext.getInstance();
                    final ValueExpression valueExpression =
                        getExpression(new BaseContext(context, parentPosition.getValue()));

                    try {
                        valueExpression.setValue(context, value);
                    } catch (PropertyNotFoundException e) {
                        if (optional) {
                            return;
                        }
                        throw e;
                    }
                }

                private ValueExpression getExpression(ELContext context) {
                    final ExpressionFactory expressionFactory = UEL.getExpressionFactory(context);
                    return expressionFactory.createValueExpression(context, UEL.join(BASE_VARIABLE_NAME, expr),
                        Object.class);
                }
            }
            return new Result(parentPosition, expr);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            return obj instanceof PositionFactory && expr.equals(((PositionFactory<?>) obj).expr);
        }

        @Override
        public int hashCode() {
            return Objects.hash(expr);
        }

        @Override
        public String toString() {
            return String.format("Expression %s", UEL.embed(expr));
        }
    }

    /**
     * Create a {@link Expression.PositionFactory} for the specified expression.
     *
     * @param expr
     * @return {@link PositionFactory}
     */
    public static <T> PositionFactory<T> at(String expr) {
        return new PositionFactory<>(Validate.notEmpty(expr, "expr"));
    }

    /**
     * Create a {@link Expression.PositionFactory} for an optional expression. A position created from such a factory
     * will silently swallow a {@link PropertyNotFoundException} and return {@code null} as its value.
     *
     * @param expr
     * @return {@link PositionFactory}
     */
    public static <T> PositionFactory<T> optional(String expr) {
        final boolean optional = true;
        return new PositionFactory<>(Validate.notEmpty(expr, "expr"), optional);
    }
}
