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

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.ConstructorUtils;

import therian.Operation;
import therian.TherianContext;
import therian.operation.Convert;
import therian.operation.Copy;
import therian.position.Position;

/**
 * Abstract base class for a converter that defers its work to a {@link Copy} {@link Operation}.
 */
public abstract class CopyingConverter<SOURCE, TARGET> extends Converter<SOURCE, TARGET> {

    public final void perform(final Convert<? extends SOURCE, ? super TARGET> convert) {
        final TARGET target;
        try {
            target = createCopyDestination(convert.getSourcePosition());
            // make result available to any concurrent equivalent conversions:
            convert.getTargetPosition().setValue(target);
        } catch (Exception e) {
            return;
        }
        final Position.Readable<TARGET> targetPosition = new Position.Readable<TARGET>() {

            public Type getType() {
                return convert.getTargetPosition().getType();
            }

            public TARGET getValue() {
                return target;
            }

        };
        TherianContext.getRequiredInstance().forwardTo(Copy.to(targetPosition, convert.getSourcePosition()));
    }

    /**
     * Create copy destination from source object.
     * 
     * @param readable
     *            object
     * @return TARGET
     */
    protected abstract TARGET createCopyDestination(Position.Readable<? extends SOURCE> readable) throws Exception;

    /**
     * Create a CopyingConverter instance that instantiates the target type using the default constructor.
     * 
     * @param targetType
     *            , must have an accessible no-arg constructor
     * @param <TARGET>
     * @return CopyingConverter instance
     */
    public static <TARGET> CopyingConverter<Object, TARGET> forTargetType(Class<TARGET> targetType) {
        final Constructor<TARGET> constructor = Validate.notNull(ConstructorUtils.getAccessibleConstructor(targetType));

        return new CopyingConverter<Object, TARGET>() {

            @Override
            protected TARGET createCopyDestination(Position.Readable<? extends Object> readable) throws Exception {
                return constructor.newInstance();
            }
        };
    }
}
