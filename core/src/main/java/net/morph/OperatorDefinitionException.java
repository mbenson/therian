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

/**
 * Exception thrown to signify a problem with the definition of an {@link Operator}.
 */
public class OperatorDefinitionException extends RuntimeException {
    /** Serialization version */
    private static final long serialVersionUID = -7571972458679149796L;

    private final Operator<?> operator;

    public OperatorDefinitionException(Operator<?> operator) {
        this.operator = operator;
    }

    public OperatorDefinitionException(Operator<?> operator, String message, Object... args) {
        super(String.format(message, args));
        this.operator = operator;
    }

    public OperatorDefinitionException(Operator<?> operator, Throwable throwable, String message, Object... args) {
        super(String.format(message, args), throwable);
        this.operator = operator;
    }

    public OperatorDefinitionException(Operator<?> operator, Throwable throwable) {
        super(throwable);
        this.operator = operator;
    }

    public Operator<?> getOperator() {
        return operator;
    }
}
