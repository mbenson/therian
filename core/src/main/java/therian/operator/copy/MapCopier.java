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
package therian.operator.copy;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import therian.TherianContext;
import therian.Operator.DependsOn;
import therian.buildweaver.StandardOperator;
import therian.operation.Add;
import therian.operation.Convert;
import therian.operation.Copy;
import therian.operator.add.AddEntryToMap;
import therian.position.Position;
import therian.util.Positions;
import therian.util.Types;

/**
 * Copies between maps, handling supported conversions between keys/values.
 */
@SuppressWarnings("rawtypes")
@StandardOperator
@DependsOn(AddEntryToMap.class)
public class MapCopier extends Copier<Map, Map> {
    private static final TypeVariable<?> KEY = Map.class.getTypeParameters()[0];
    private static final TypeVariable<?> VALUE = Map.class.getTypeParameters()[1];
    private static final Map.Entry<?, ?> EMPTY_ENTRY = Pair.of(null, null);

    @SuppressWarnings("unchecked")
    @Override
    public boolean perform(TherianContext context, Copy<? extends Map, ? extends Map> copy) {
        final Map<TypeVariable<?>, Type> sourceArgs =
            TypeUtils.getTypeArguments(copy.getSourceType().getType(), Map.class);
        final Map<TypeVariable<?>, Type> targetArgs =
            TypeUtils.getTypeArguments(copy.getTargetType().getType(), Map.class);

        final Type sourceKeyType = Types.unrollVariables(sourceArgs, KEY);
        final Type sourceValueType = Types.unrollVariables(sourceArgs, VALUE);
        final Type targetKeyType = Types.unrollVariables(targetArgs, KEY);
        final Type targetValueType = Types.unrollVariables(targetArgs, VALUE);

        final Type targetEntryType;
        if (targetKeyType != null && targetValueType != null) {
            targetEntryType = Types.parameterize(Map.Entry.class, targetKeyType, targetValueType);
        } else {
            targetEntryType = Map.Entry.class;
        }

        final MutablePair<Object, Object> newEntry = MutablePair.of(null, null);
        final Position.Readable<Map.Entry> targetElement = Positions.<Map.Entry> readOnly(targetEntryType, newEntry);
        final Position.Writable<?> targetKey = new Position.Writable() {

            @Override
            public Type getType() {
                return targetKeyType;
            }

            @Override
            public void setValue(Object value) {
                newEntry.setLeft(value);
            }
        };

        final Position.Writable<?> targetValue = new Position.Writable() {

            @Override
            public Type getType() {
                return targetValueType;
            }

            @Override
            public void setValue(Object value) {
                newEntry.setRight(value);
            }
        };

        final Position.ReadWrite sourceKey = Positions.readWrite(sourceKeyType);
        final Position.ReadWrite sourceValue = Positions.readWrite(sourceValueType);

        final Map<?, ?> sourceMap = copy.getSourcePosition().getValue();
        for (Map.Entry<?, ?> e : sourceMap.entrySet()) {
            sourceKey.setValue(e.getKey());
            if (!context.evalSuccess(Convert.to(targetKey, sourceKey))) {
                return false;
            }
            sourceValue.setValue(e.getValue());
            if (!context.evalSuccess(Convert.to(targetValue, sourceValue))) {
                return false;
            }
            if (!context.evalSuccess(Add.to(copy.getTargetPosition(), targetElement))) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean supports(TherianContext context, Copy<? extends Map, ? extends Map> copy) {
        if (!super.supports(context, copy)) {
            return false;
        }
        final Map<?, ?> sourceMap = copy.getSourcePosition().getValue();
        if (sourceMap == null) {
            return false;
        }
        final Map<TypeVariable<?>, Type> sourceArgs =
            TypeUtils.getTypeArguments(copy.getSourceType().getType(), Map.class);
        final Map<TypeVariable<?>, Type> targetArgs =
            TypeUtils.getTypeArguments(copy.getTargetType().getType(), Map.class);

        final Type sourceKeyType = Types.unrollVariables(sourceArgs, KEY);
        final Type sourceValueType = Types.unrollVariables(sourceArgs, VALUE);
        final Type targetKeyType = Types.unrollVariables(targetArgs, KEY);
        final Type targetValueType = Types.unrollVariables(targetArgs, VALUE);

        final Type targetEntryType;
        if (targetKeyType != null && targetValueType != null) {
            targetEntryType = Types.parameterize(Map.Entry.class, targetKeyType, targetValueType);
        } else {
            targetEntryType = Map.Entry.class;
        }
        // assume that if we can add a single entry we can add them all :|
        if (!context.supports(Add.to(copy.getTargetPosition(),
            Positions.<Map.Entry> readOnly(targetEntryType, EMPTY_ENTRY)))) {
            return false;
        }

        final Position.ReadWrite sourceKey =
            Positions.readWrite(ObjectUtils.defaultIfNull(sourceKeyType, Object.class));
        final Position.ReadWrite sourceValue =
            Positions.readWrite(ObjectUtils.defaultIfNull(sourceValueType, Object.class));
        final Position.Writable<?> targetKey =
            Positions.writable(ObjectUtils.defaultIfNull(targetKeyType, Object.class));
        final Position.Writable<?> targetValue =
            Positions.writable(ObjectUtils.defaultIfNull(targetValueType, Object.class));

        for (Map.Entry<?, ?> e : sourceMap.entrySet()) {
            sourceKey.setValue(e.getKey());
            sourceValue.setValue(e.getValue());
            if (!context.supports(Convert.to(targetKey, sourceKey))) {
                return false;
            }
            if (!context.supports(Convert.to(targetValue, sourceValue))) {
                return false;
            }
        }
        return true;
    }
}
