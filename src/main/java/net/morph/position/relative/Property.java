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
package net.morph.position.relative;

import java.lang.reflect.Type;

import net.morph.MorphContext;
import net.morph.position.Position;
import net.morph.position.Position.Readable;
import net.morph.position.relative.RelativePosition.ReadWrite;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

public class Property<T> extends RelativePositionFactory<T> {
    private final String propertyName;

    @SuppressWarnings("unchecked")
    private Property(final String propertyName) {
        super(new RelativePosition.GetType<T>() {

            public <P> Type getType(Position.Readable<? extends P> parentPosition) {
                // TODO improve
                final MorphContext context = MorphContext.getInstance();
                final Class<?> type = context.getELResolver().getType(context, parentPosition.getValue(), propertyName);
                Validate.validState(context.isPropertyResolved(), "could not resolve type of %s from %s", propertyName,
                    parentPosition);
                return type;
            }

        }, new RelativePosition.GetValue<T>() {

            public <P> T getValue(Position.Readable<? extends P> parentPosition) {
                // TODO improve
                final MorphContext context = MorphContext.getInstance();
                final Object value = context.getELResolver().getValue(context, parentPosition.getValue(), propertyName);
                Validate.validState(context.isPropertyResolved(), "could not get value %s from %s", propertyName,
                    parentPosition);
                return (T) value;
            }

        }, new RelativePosition.SetValue<T>() {

            public <P> void setValue(Position.Readable<? extends P> parentPosition, T value) {
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

    @Override
    public <P> RelativePosition.ReadWrite<P, T> of(Readable<P> parentPosition) {
        return (ReadWrite<P, T>) super.of(parentPosition);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Property == false) {
            return false;
        }
        return StringUtils.equals(((Property<?>) obj).propertyName, propertyName);
    }
    
    @Override
    public int hashCode() {
        return (71 << 4) | propertyName.hashCode();
    }
    
    public static <T> Property<T> at(String propertyName) {
        return new Property<T>(Validate.notNull(propertyName, "propertyName"));
    }
}
