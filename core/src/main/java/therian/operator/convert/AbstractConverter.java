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
package therian.operator.convert;

import org.apache.commons.lang3.reflect.TypeUtils;

import therian.Operator;
import therian.operation.Convert;
import therian.operator.FromSourceToTarget;
import therian.util.Positions;

/**
 * Abstract {@link Convert} {@link Operator} superclass.
 */
public abstract class AbstractConverter extends FromSourceToTarget {

    /**
     * Learn whether a {@link Convert} operation's source value is (already) an instance of its target type,
     *
     * @param convert
     * @return boolean
     */
    protected final boolean isNoop(Convert<?, ?> convert) {
        return TypeUtils.isAssignable(Positions.narrowestParameterizedType(convert.getSourcePosition()), convert
            .getTargetPosition().getType());
    }

    /**
     * Should this {@link AbstractConverter} detect (and reject) conversions can, by virtue of source value/target type
     * assignability, can be considered noops?
     *
     * @return true
     */
    protected boolean isRejectNoop() {
        return true;
    }
}
