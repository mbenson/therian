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
package therian;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import therian.operator.add.AddToCollection;
import therian.operator.add.AddToListIterator;
import therian.operator.addall.GenericAddAllOperator;
import therian.operator.convert.CopyingConverter;
import therian.operator.convert.DefaultCopyingConverter;
import therian.operator.convert.DefaultToIteratorConverter;
import therian.operator.convert.DefaultToListConverter;
import therian.operator.convert.ELCoercionConverter;
import therian.operator.convert.EnumToNumberConverter;
import therian.operator.convert.EnumerationToIterator;
import therian.operator.convert.EnumerationToList;
import therian.operator.convert.IterableToEnumeration;
import therian.operator.convert.IterableToIterator;
import therian.operator.convert.IteratorToEnumeration;
import therian.operator.convert.IteratorToList;
import therian.operator.convert.MapToValues;
import therian.operator.convert.NOPConverter;
import therian.operator.copy.BeanCopier;
import therian.operator.copy.ConvertingCopier;
import therian.operator.getelementtype.GetArrayElementType;
import therian.operator.getelementtype.GetEnumerationElementType;
import therian.operator.getelementtype.GetIterableElementType;
import therian.operator.getelementtype.GetIteratorElementType;
import therian.operator.immutablecheck.DefaultImmutableChecker;
import therian.operator.size.DefaultSizeOperator;
import therian.operator.size.SizeOfCharSequence;
import therian.operator.size.SizeOfCollection;
import therian.operator.size.SizeOfIterable;
import therian.operator.size.SizeOfIterator;
import therian.operator.size.SizeOfMap;
import therian.util.Types;

/**
 * Utility methods for Operators.
 */
public class Operators {

    // @formatter:off
    private static final Operator<?>[] STANDARD_OPERATORS = {
        /*
         * TODO add more
         */
        //Add
        new AddToCollection(),
        new AddToListIterator(),

        //AddAll
        new GenericAddAllOperator(),
        
        //Convert
        new ELCoercionConverter(),
        new DefaultCopyingConverter(),

        // these can't actually work until a mechanism for copying to target
        // interfaces is in place:
        CopyingConverter.implementing(List.class).with(ArrayList.class),
        CopyingConverter.implementing(Map.class).with(HashMap.class),
        CopyingConverter.implementing(Set.class).with(HashSet.class),
        CopyingConverter.implementing(SortedSet.class).with(TreeSet.class),
        CopyingConverter.implementing(SortedMap.class).with(TreeMap.class),

        new DefaultToListConverter(),
        new DefaultToIteratorConverter(),
        new EnumerationToIterator(),
        new EnumerationToList(),
        new EnumToNumberConverter(),
        new IterableToEnumeration(),
        new IteratorToEnumeration(),
        new IterableToIterator(),
        new IterableToEnumeration(),
        new IteratorToList(),
        new MapToValues(),
        new NOPConverter(),

        //Copy
        new BeanCopier(),
        new ConvertingCopier(),

        //GetElementType
        new GetArrayElementType(),
        new GetEnumerationElementType(),
        new GetIterableElementType(),
        new GetIteratorElementType(),
        
        //ImmutableCheck
        new DefaultImmutableChecker(),

        //Size
        new DefaultSizeOperator(),
        new SizeOfCharSequence(),
        new SizeOfCollection(),
        new SizeOfIterable(),
        new SizeOfIterator(),
        new SizeOfMap()
    };
    // @formatter:on

    private static final Comparator<Operator<?>> COMPARATOR = new Comparator<Operator<?>>() {

        public int compare(Operator<?> o1, Operator<?> o2) {
            final Type opType1 =
                TypeUtils.getTypeArguments(o1.getClass(), Operator.class).get(Operator.class.getTypeParameters()[0]);
            final Type opType2 =
                TypeUtils.getTypeArguments(o2.getClass(), Operator.class).get(Operator.class.getTypeParameters()[0]);

            if (ObjectUtils.equals(opType1, opType2)) {
                return 0;
            }
            if (TypeUtils.isAssignable(opType1, opType2)) {
                return -1;
            }
            if (TypeUtils.isAssignable(opType2, opType1)) {
                return 1;
            }
            final Class<?> raw1 = TypeUtils.getRawType(opType1, o1.getClass());
            final Class<?> raw2 = TypeUtils.getRawType(opType2, o2.getClass());
            if (ObjectUtils.equals(raw1, raw2)) {
                return compareTypes(ImmutablePair.of(opType1, o1.getClass()), ImmutablePair.of(opType2, o2.getClass()));
            }
            return opType1.toString().compareTo(opType2.toString());
        }

        /**
         * Compare types
         * 
         * @param p1 first pair of type, assigning type
         * @param p2 second pair of type, assigning type
         * @return int
         */
        private int compareTypes(ImmutablePair<? extends Type, ? extends Type> p1,
            ImmutablePair<? extends Type, ? extends Type> p2) {
            if (ObjectUtils.equals(p1.left, p2.left)) {
                return 0;
            }
            if (TypeUtils.isAssignable(p1.left, p2.left)) {
                return -1;
            }
            if (TypeUtils.isAssignable(p2.left, p1.left)) {
                return 1;
            }
            final Class<?> raw1 = TypeUtils.getRawType(p1.left, p1.right);
            final Class<?> raw2 = TypeUtils.getRawType(p2.left, p2.right);
            if (ObjectUtils.equals(raw1, raw2)) {
                if (raw1.getTypeParameters().length == 0) {
                    return 0;
                }
                final Map<TypeVariable<?>, Type> typeArgs1 = TypeUtils.getTypeArguments(p1.left, raw1);
                final Map<TypeVariable<?>, Type> typeArgs2 = TypeUtils.getTypeArguments(p2.left, raw2);
                for (TypeVariable<?> var : raw1.getTypeParameters()) {
                    final int recurse =
                        compareTypes(ImmutablePair.of(typeArgs1.get(var), p1.right),
                            ImmutablePair.of(typeArgs2.get(var), p2.right));
                    if (recurse != 0) {
                        return recurse;
                    }
                }
                return 0;
            }
            return p1.left.toString().compareTo(p2.left.toString());
        }
    };

    /**
     * Get standard operators.
     * 
     * @return Operator[]
     */
    public static Operator<?>[] standard() {
        return STANDARD_OPERATORS;
    }

    /**
     * Validate an {@link Operator} implementation.
     * 
     * @param operator
     * @param <OPERATOR>
     * @return {@code operator}
     * @throws OperatorDefinitionException on invalid operator
     */
    public static <OPERATOR extends Operator<?>> OPERATOR validateImplementation(OPERATOR operator) {
        for (TypeVariable<?> var : Validate.notNull(operator, "operator").getClass().getTypeParameters()) {
            if (Types.resolveAt(operator, var) == null) {
                throw new OperatorDefinitionException(operator, "Could not resolve %s.%s against operator %s",
                    var.getGenericDeclaration(), var.getName(), operator);
            }
        }
        return operator;
    }

    /**
     * Get a comparator that compares {@link Operator}s by {@link Operation} type/type parameter assignability.
     * 
     * @return a Comparator that does not handle {@code null} values
     */
    public static Comparator<Operator<?>> comparator() {
        return COMPARATOR;
    }
}
