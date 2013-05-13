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
package therian.uelbox;

import java.beans.FeatureDescriptor;
import java.util.Iterator;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;

/**
 * UEL utility methods.
 */
public class UEL {
    private static final ELResolver NOP_EL_RESOLVER = new ELResolver() {

        @Override
        public Class<?> getCommonPropertyType(ELContext arg0, Object arg1) {
            return null;
        }

        @Override
        public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext arg0, Object arg1) {
            return null;
        }

        @Override
        public Class<?> getType(ELContext arg0, Object arg1, Object arg2) throws NullPointerException,
            PropertyNotFoundException, ELException {
            return null;
        }

        @Override
        public Object getValue(ELContext arg0, Object arg1, Object arg2) throws NullPointerException,
            PropertyNotFoundException, ELException {
            return null;
        }

        @Override
        public boolean isReadOnly(ELContext arg0, Object arg1, Object arg2) throws NullPointerException,
            PropertyNotFoundException, ELException {
            return false;
        }

        @Override
        public void setValue(ELContext arg0, Object arg1, Object arg2, Object arg3) throws NullPointerException,
            PropertyNotFoundException, PropertyNotWritableException, ELException {
        }

    };

    /**
     * Get an ExpressionFactory instance for the specified context, using {@link ELContext#getContext(Class)}, and
     * setting such a context value, if not found, to {@link javax.el.ExpressionFactory#newInstance()}.
     *
     * @param elContext
     * @return ExpressionFactory
     */
    public static ExpressionFactory getExpressionFactory(ELContext context) {
        ExpressionFactory result = getContext(context, ExpressionFactory.class);
        if (result == null) {
            result = ExpressionFactory.newInstance();
            context.putContext(ExpressionFactory.class, result);
        }
        return result;
    }

    /**
     * Casts context objects per documented convention.
     *
     * @param context
     * @param key
     * @param <T>
     * @return T
     */
    public static <T> T getContext(ELContext context, Class<T> key) {
        @SuppressWarnings("unchecked")
        final T result = (T) context.getContext(key);
        return result;
    }

    /**
     * Embed the specified expression, if necessary, using '#' as the triggering character.
     *
     * @param expression
     * @return String
     */
    public static String embed(String expression) {
        return embed(expression, '#');
    }

    /**
     * Embed the specified expression, if necessary, using the specified triggering character.
     *
     * @param expression
     * @param trigger
     * @return String
     */
    public static String embed(String expression, char trigger) {
        if (expression.matches("^.\\{(.*)\\}$")) {
            if (expression.charAt(0) == trigger) {
                return expression;
            }
            expression = expression.substring(2, expression.length() - 1);
        }
        return new StringBuilder(String.format("{%s}", expression)).insert(0, trigger).toString();
    }

    /**
     * Use EL specification coercion facilities to coerce an object to the specified type.
     *
     * @param context
     * @param toType
     * @param object
     * @return T
     * @throws ELException if the coercion fails.
     */
    public static <T> T coerceToType(ELContext context, Class<T> toType, Object object) {
        @SuppressWarnings("unchecked")
        T result = (T) getExpressionFactory(context).coerceToType(object, toType);
        return result;
    }

    /**
     * Get an {@link ELResolver} that handles nothing.
     *
     * @return ELResolver
     */
    public static ELResolver nopELResolver() {
        return NOP_EL_RESOLVER;
    }
}
