package therian.operator.copy;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.reflect.TypeUtils;

import therian.BindTypeVariable;
import therian.Operator.DependsOn;
import therian.TherianContext;
import therian.Typed;
import therian.buildweaver.StandardOperator;
import therian.operation.AddAll;
import therian.operation.Convert;
import therian.operation.Copy;
import therian.operation.GetElementType;
import therian.operator.add.AddToCollection;
import therian.operator.add.AddToListIterator;
import therian.operator.addall.AddAllToArray;
import therian.operator.addall.GenericAddAllOperator;
import therian.operator.convert.DefaultToArrayConverter;
import therian.operator.convert.DefaultToListConverter;
import therian.operator.convert.EnumerationToList;
import therian.operator.convert.IteratorToList;
import therian.operator.convert.NOPConverter;
import therian.position.Position;
import therian.position.relative.Element;
import therian.util.Positions;
import therian.util.Types;

/**
 * Tries to convert source and target to {@link Iterable}s, copy source elements onto corresponding target elements,
 * then to add remaining elements to target. If elements cannot be added but target position is writable, fallback
 * strategy is to add all target elements to a new array of target element type, and attempt to convert that to the
 * target position. This class contains the hidden gem that is nested element conversion.
 */
@DependsOn({ DefaultToListConverter.class, NOPConverter.class, DefaultToArrayConverter.class,
    GenericAddAllOperator.class, AddAllToArray.class })
public abstract class ContainerCopier<TARGET> extends Copier<Object, TARGET> {

    @SuppressWarnings("rawtypes")
    @StandardOperator
    @DependsOn(AddToCollection.class)
    public static class ToIterable extends ContainerCopier<Iterable> {
    }

    @SuppressWarnings("rawtypes")
    @StandardOperator
    @DependsOn({ IteratorToList.class, AddToListIterator.class })
    public static class ToIterator extends ContainerCopier<Iterator> {
    }

    @SuppressWarnings("rawtypes")
    @StandardOperator
    @DependsOn({ EnumerationToList.class })
    public static class ToEnumeration extends ContainerCopier<Enumeration> {
    }

    @StandardOperator
    public static class ToArray extends ContainerCopier<Object> {
        @Override
        public boolean supports(TherianContext context, Copy<?, ? extends Object> copy) {
            return TypeUtils.isArrayType(copy.getTargetPosition().getType()) && super.supports(context, copy);
        }
    }

    public static abstract class Dynamic<TARGET> extends ContainerCopier<TARGET> {
        private Dynamic() {
        }

        @BindTypeVariable
        public abstract Typed<TARGET> getTargetType();
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public boolean perform(final TherianContext context, final Copy<?, ? extends TARGET> copy) {
        final Iterable sourceIterable = context.eval(Convert.to(Iterable.class, copy.getSourcePosition()));
        final Iterator<?> sourceIterator = sourceIterable.iterator();
        final Iterable targetIterable = context.eval(Convert.to(Iterable.class, copy.getTargetPosition()));
        final Iterator<?> targetIterator = targetIterable.iterator();
        final Type targetElementType = context.eval(GetElementType.of(copy.getTargetPosition()));
        final Type sourceElementType = context.eval(GetElementType.of(copy.getSourcePosition()));

        while (targetIterator.hasNext()) {
            if (!sourceIterator.hasNext()) {
                break;
            }
            final Object targetElement = targetIterator.next();
            final Object sourceElement = sourceIterator.next();
            if (sourceElement != null) {
                if (!context.evalSuccess(Copy.to(Positions.readOnly(targetElement),
                    Positions.readOnly(sourceElementType, sourceElement)))) {
                    return false;
                }
            }
        }
        if (!sourceIterator.hasNext()) {
            // at this point we have copied all source elements onto existing target elements without running out
            // of target elements to copy onto, so we're good
            return true;
        }
        final List<Object> sourceElementsForConversion = new ArrayList<Object>();
        while (sourceIterator.hasNext()) {
            sourceElementsForConversion.add(sourceIterator.next());
        }

        final Position.ReadWrite<?> targetElements = Positions.readWrite(Types.genericArrayType(targetElementType));
        final Class<?> rawTargetElementType = TypeUtils.getRawType(targetElementType, null);
        ((Position.Writable) targetElements).setValue(Array.newInstance(rawTargetElementType,
            sourceElementsForConversion.size()));

        final Position.Readable<List<?>> sourceSubList =
            Positions
                .<List<?>> readOnly(Types.parameterize(List.class, sourceElementType), sourceElementsForConversion);

        for (int i = 0, sz = sourceElementsForConversion.size(); i < sz; i++) {
            final Position.ReadWrite<?> targetElement = Element.atArrayIndex(i).of(targetElements);
            final Position.ReadWrite<?> sourceElement = Element.atIndex(i).of(sourceSubList);

            if (!context.evalSuccess(Convert.to(targetElement, sourceElement))) {
                return false;
            }
        }

        final AddAll<?, ?> addAll = AddAll.to(copy.getTargetPosition(), targetElements);
        if (context.supports(addAll)) {
            return context.forwardTo(addAll);
        }

        // can't add new elements. last try: convert an array of the proper size to the target type and set value
        if (!Positions.isWritable(copy.getTargetPosition())) {
            return false;
        }
        final List<Object> allElements = new ArrayList<Object>();
        // add original elements
        for (Object t : targetIterable) {
            allElements.add(t);
        }
        // add target elements converted from source objects
        for (int i = 0, sz = Array.getLength(targetElements.getValue()); i < sz; i++) {
            allElements.add(Array.get(targetElements.getValue(), i));
        }
        ((Position.Writable) targetElements).setValue(allElements.toArray((Object[]) Array.newInstance(
            TypeUtils.getRawType(targetElementType, null), allElements.size())));

        final Position.Writable<?> convertTarget = (Position.Writable<?>) copy.getTargetPosition();

        return context.forwardTo(Convert.to(convertTarget, targetElements));
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public boolean supports(final TherianContext context, final Copy<?, ? extends TARGET> copy) {
        if (!super.supports(context, copy)) {
            return false;
        }
        final GetElementType<?> getSourceElementType = GetElementType.of(copy.getSourcePosition());
        if (!context.supports(getSourceElementType)) {
            return false;
        }

        final GetElementType<?> getTargetElementType = GetElementType.of(copy.getTargetPosition());
        if (!context.supports(getTargetElementType)) {
            return false;
        }

        final Iterable sourceIterable = context.evalIfSupported(Convert.to(Iterable.class, copy.getSourcePosition()));
        if (sourceIterable == null) {
            return false;
        }
        final Iterable targetIterable = context.evalIfSupported(Convert.to(Iterable.class, copy.getTargetPosition()));
        if (targetIterable == null) {
            return false;
        }
        final Type sourceElementType = context.eval(getSourceElementType);
        final Type targetElementType = context.eval(getTargetElementType);

        final Iterator<?> sourceIterator = sourceIterable.iterator();
        final Iterator<?> targetIterator = targetIterable.iterator();

        while (targetIterator.hasNext()) {
            if (!sourceIterator.hasNext()) {
                break;
            }
            final Object targetElement = targetIterator.next();
            final Object sourceElement = sourceIterator.next();
            if (targetElement == null && sourceElement != null) {
                // can do nothing with null target elements returned from an Iterator:
                return false;
            }
            if (sourceElement != null) {
                final Position.Readable<?> sourceElementPosition = Positions.readOnly(sourceElementType, sourceElement);
                if (!context.supports(Copy.to(Positions.readOnly(targetElement), sourceElementPosition))) {
                    return false;
                }
            }
        }
        if (!sourceIterator.hasNext()) {
            return true;
        }
        final List<Object> sourceElementsForConversion = new ArrayList<Object>();
        while (sourceIterator.hasNext()) {
            sourceElementsForConversion.add(sourceIterator.next());
        }

        //@formatter:off
        /*
         * array is "lowest common denominator" wrt primitive vs. Object elements;
         * plan:
         *  - create an array of proper size and initial values
         *  - verify we can convert each remaining element into the corresponding position of this array
         *  - verify that we can either:
         *    - AddAll this array to our original target pos, OR
         *    - expand the array to full size and convert to original (writable) target pos
         */
        //@formatter:on

        final Class<?> rawTargetElementType = TypeUtils.getRawType(targetElementType, null);

        final Position.ReadWrite<?> targetElements = Positions.readWrite(Types.genericArrayType(targetElementType));
        ((Position.Writable) targetElements).setValue(Array.newInstance(rawTargetElementType,
            sourceElementsForConversion.size()));

        final Type sourceSubListType =
            sourceElementType == null ? List.class : Types.parameterize(List.class, sourceElementType);
        final Position.Readable<List<?>> sourceSubList =
            Positions.<List<?>> readOnly(sourceSubListType, sourceElementsForConversion);

        for (int i = 0, sz = sourceElementsForConversion.size(); i < sz; i++) {
            final Position.ReadWrite<?> targetElement = Element.atArrayIndex(i).of(targetElements);
            final Position.ReadWrite<?> sourceElement = Element.atIndex(i).of(sourceSubList);

            if (!context.supports(Convert.to(targetElement, sourceElement))) {
                return false;
            }
        }

        if (context.supports(AddAll.to(copy.getTargetPosition(), targetElements))) {
            return true;
        }

        // can't add new elements. last try: can we convert an array of the proper size to the target type and set
        // value?
        if (!Positions.isWritable(copy.getTargetPosition())) {
            return false;
        }
        final List<Object> allElements = new ArrayList<Object>();
        for (Object t : targetIterable) {
            allElements.add(t);
        }
        for (@SuppressWarnings("unused")
        Object s : sourceElementsForConversion) {
            allElements.add(null);
        }
        ((Position.Writable) targetElements).setValue(allElements.toArray((Object[]) Array.newInstance(
            rawTargetElementType, allElements.size())));

        return context.supports(Convert.to((Position.Writable<?>) copy.getTargetPosition(), targetElements));
    }

    @Override
    protected boolean isRejectImmutable() {
        return false;
    }

    public static <TARGET> ContainerCopier.Dynamic<TARGET> to(final Typed<TARGET> targetType) {
        return new ContainerCopier.Dynamic<TARGET>() {

            @Override
            public Typed<TARGET> getTargetType() {
                return targetType;
            }
        };
    }
}
