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
package therian.operator;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import org.apache.commons.lang3.reflect.TypeUtils;

import therian.util.Types;

/**
 * Manages source/target bounds.
 */
public abstract class FromSourceToTarget {

    /**
     * Describes some type that has a source.
     * 
     * @param <S>
     */
    public interface FromSource<S> {
    }

    /**
     * Describes some type that has a target.
     * 
     * @param <T>
     */
    public interface ToTarget<T> {
    }

    private static final TypeVariable<?> SOURCE = FromSource.class.getTypeParameters()[0];
    private static final TypeVariable<?> TARGET = ToTarget.class.getTypeParameters()[0];

    private final Type sourceBound;
    private final Type targetBound;

    {
        // try to set these once from class info alone, skipping @BindTypeVariable stuff:
        sourceBound = TypeUtils.unrollVariables(TypeUtils.getTypeArguments(getClass(), FromSource.class), SOURCE);
        targetBound = TypeUtils.unrollVariables(TypeUtils.getTypeArguments(getClass(), ToTarget.class), TARGET);
    }

    /**
     * Get the Type detected for type parameter {@code SOURCE}.
     * 
     * @return Type
     */
    protected Type getSourceBound() {
        return sourceBound == null ? Types.resolveAt(this, SOURCE) : sourceBound;
    }

    /**
     * Get the Type detected for type parameter {@code TARGET}.
     * 
     * @return Type
     */
    protected Type getTargetBound() {
        return targetBound == null ? Types.resolveAt(this, TARGET) : targetBound;
    }

    /**
     * {@inheritDoc}
     * 
     * By default, any instance that fully binds type parameters is considered equal to an instance of the same class.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        return obj.getClass().equals(getClass()) && sourceBound != null && targetBound != null;
    }
}
