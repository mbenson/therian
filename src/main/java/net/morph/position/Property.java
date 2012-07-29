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
package net.morph.position;

import java.lang.reflect.Type;

import org.apache.commons.lang3.Validate;

import net.morph.MorphContext;

public class Property<P, T, POSITION_TYPE extends Position.Readable<T> & Position.Writable<T>> extends
    RelativePositionFactory<P, T, POSITION_TYPE> {
    private final String propertyName;

    @SuppressWarnings("unchecked")
    private Property(final String propertyName) {
        super(new RelativePosition.GetType<P, T>() {

            public Type getType(Position.Readable<? extends P> parentPosition) {
                // TODO improve
                final MorphContext context = MorphContext.getInstance();
                final Class<?> type = context.getELResolver().getType(context, parentPosition.getValue(), propertyName);
                Validate.validState(context.isPropertyResolved(), "could not resolve type of %s from %s", propertyName,
                    parentPosition);
                return type;
            }

        }, new RelativePosition.GetValue<P, T>() {

            public T getValue(Position.Readable<? extends P> parentPosition) {
                // TODO improve
                final MorphContext context = MorphContext.getInstance();
                final Object value = context.getELResolver().getValue(context, parentPosition.getValue(), propertyName);
                Validate.validState(context.isPropertyResolved(), "could not get value %s from %s", propertyName,
                    parentPosition);
                return (T) value;
            }

        }, new RelativePosition.SetValue<P, T>() {

            public void setValue(Position.Readable<? extends P> parentPosition, T value) {
                // TODO improve
                final MorphContext context = MorphContext.getInstance();
                context.getELResolver().setValue(context, parentPosition.getValue(), propertyName, value);
                Validate.validState(context.isPropertyResolved(), "could not set value %s onto %s from %s", value,
                    propertyName, parentPosition);
            }

        });
        this.propertyName = propertyName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public static <P, T, POSITION_TYPE extends Position.Readable<T> & Position.Writable<T>> Property<P, T, POSITION_TYPE> at(
        String propertyName) {
        return new Property<P, T, POSITION_TYPE>(propertyName);
    }
}
