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
import javax.el.ExpressionFactory;

/**
 * UEL utility methods.
 */
public class UEL {
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
}
