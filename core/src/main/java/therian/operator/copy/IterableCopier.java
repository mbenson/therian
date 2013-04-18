package therian.operator.copy;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.reflect.TypeUtils;

import therian.Operator;
import therian.TherianContext;
import therian.operation.AddAll;
import therian.operation.Convert;
import therian.operation.Copy;
import therian.operation.GetElementType;
import therian.position.Box;
import therian.position.Position;
import therian.position.Ref;
import therian.util.Types;

/**
 * Tries to convert source and target to iterables and copy source elements onto corresponding target elements,
 * then to add remaining elements to target.
 */
public class IterableCopier implements Operator<Copy<?, ?>> {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void perform(final TherianContext context, final Copy<?, ?> copy) {
        final GetElementType<?> getTargetElementType = GetElementType.of(copy.getTargetPosition());
        final Type targetElementType = context.eval(getTargetElementType);

        final Convert<?, Iterable> sourceToIterable = Convert.to(Iterable.class, copy.getSourcePosition());
        final Convert<?, Iterable> targetToIterable = Convert.to(Iterable.class, copy.getTargetPosition());

        final Iterable<?> sourceIterable = context.eval(sourceToIterable);
        final Iterator<?> sourceIterator = sourceIterable.iterator();
        final Iterable<?> targetIterable = context.eval(targetToIterable);
        final Iterator<?> targetIterator = targetIterable.iterator();
        final Type sourceElementType;
        final GetElementType<?> getSourceElementType = GetElementType.of(copy.getSourcePosition());
        if (context.supports(getSourceElementType)) {
            sourceElementType = context.eval(getSourceElementType);
        } else {
            sourceElementType = null;
        }
        while (targetIterator.hasNext()) {
            if (!sourceIterator.hasNext()) {
                break;
            }
            final Object targetElement = targetIterator.next();
            final Object sourceElement = sourceIterator.next();
            if (sourceElement != null) {
                final Position.Readable<?> sourceElementPosition;
                if (sourceElementType == null || sourceElementType instanceof Class<?>) {
                    sourceElementPosition = Ref.to(sourceElement);
                } else {
                    sourceElementPosition = new Box<Object>(sourceElementType, sourceElement);
                }
                final Copy<?, ?> copyElement = Copy.to(Ref.to(targetElement), sourceElementPosition);
                context.eval(copyElement);
                if (!copyElement.isSuccessful()) {
                    return;
                }
            }
        }
        if (!sourceIterator.hasNext()) {
            // at this point we have copied all source elements onto existing target elements without running out
            // of target elements to copy onto, so we're good
            copy.setSuccessful(true);
            return;
        }
        final List<Object> sourceElementsForConversion = new ArrayList<Object>();
        while (sourceIterator.hasNext()) {
            sourceElementsForConversion.add(sourceIterator.next());
        }

        final Box<?> targetElements = new Box(Types.genericArrayType(targetElementType));
        final Type sourceSubListType =
            sourceElementType == null ? List.class : Types.parameterize(List.class, sourceElementType);
        final Box<?> sourceSubList = new Box<List<?>>(sourceSubListType, sourceElementsForConversion);
        final Convert<?, ?> toTargetElementArray = Convert.to(targetElements, sourceSubList);
        context.eval(toTargetElementArray);
        if (!toTargetElementArray.isSuccessful()) {
            return;
        }

        final AddAll<?, ?> addAll = AddAll.to(copy.getTargetPosition(), targetElements);
        if (context.supports(addAll)) {
            context.forwardTo(addAll);
        } else {

            // can't add new elements. last try: convert an array of the proper size to the target type and set value
            if (copy.getTargetPosition() instanceof Position.Writable<?> == false) {
                return;
            }
            final List<Object> allElements = new ArrayList<Object>();
            // add original elements
            for (Object t : targetIterable) {
                allElements.add(t);
            }
            // add target elements converted from source objects
            for (Object s : ((Object[]) targetElements.getValue())) {
                allElements.add(s);
            }
            ((Box) targetElements).setValue(allElements.toArray((Object[]) Array.newInstance(
                TypeUtils.getRawType(targetElements.getType(), null), allElements.size())));

            final Position.Writable<?> convertTarget = ((Position.Writable<?>) copy.getTargetPosition());

            context.forwardTo(Convert.to(convertTarget, targetElements));
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public boolean supports(final TherianContext context, final Copy<?, ?> copy) {
        // do we understand target type as a group of elements?
        final GetElementType<?> getTargetElementType = GetElementType.of(copy.getTargetPosition());
        if (!context.supports(getTargetElementType)) {
            return false;
        }

        final Type targetElementType = context.eval(getTargetElementType);

        final Convert<?, Iterable> sourceToIterable = Convert.to(Iterable.class, copy.getSourcePosition());
        if (!context.supports(sourceToIterable)) {
            return false;
        }
        final Convert<?, Iterable> targetToIterable = Convert.to(Iterable.class, copy.getTargetPosition());

        if (!context.supports(targetToIterable)) {
            return false;
        }
        final Iterable<?> sourceIterable = context.eval(sourceToIterable);
        final Iterator<?> sourceIterator = sourceIterable.iterator();
        final Iterable<?> targetIterable = context.eval(targetToIterable);
        final Iterator<?> targetIterator = targetIterable.iterator();

        final Type sourceElementType;
        final GetElementType<?> getSourceElementType = GetElementType.of(copy.getSourcePosition());
        if (context.supports(getSourceElementType)) {
            sourceElementType = context.eval(getSourceElementType);
        } else {
            sourceElementType = null;
        }
        while (targetIterator.hasNext()) {
            if (!sourceIterator.hasNext()) {
                break;
            }
            final Object targetElement = targetIterator.next();
            final Object sourceElement = sourceIterator.next();
            if (targetElement == null && sourceElement != null) {
                // can do nothing with null elements returned from an Iterator:
                return false;
            }
            if (sourceElement != null) {
                final Position.Readable<?> sourceElementPosition;
                if (sourceElementType == null || sourceElementType instanceof Class<?>) {
                    sourceElementPosition = Ref.to(sourceElement);
                } else {
                    sourceElementPosition = new Box<Object>(sourceElementType, sourceElement);
                }
                if (!context.supports(Copy.to(Ref.to(targetElement), sourceElementPosition))) {
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
        // can we convert these to an array?
        final Box<?> targetElements = new Box<Object>(Types.genericArrayType(targetElementType));
        final Type sourceSubListType =
            sourceElementType == null ? List.class : Types.parameterize(List.class, sourceElementType);
        final Box<?> sourceSubList = new Box<List<?>>(sourceSubListType, sourceElementsForConversion);
        if (!context.supports(Convert.to(targetElements, sourceSubList))) {
            return false;
        }

        // array of proper size, but null values == best we can do.
        final Class<?> rawTargetElementType = TypeUtils.getRawType(targetElements.getType(), null);

        ((Box) targetElements).setValue(Array.newInstance(rawTargetElementType, sourceElementsForConversion.size()));

        if (context.supports(AddAll.to(copy.getTargetPosition(), targetElements))) {
            return true;
        }

        // can't add new elements. last try: can we convert an array of the proper size to the target type and set
        // value?
        if (copy.getTargetPosition() instanceof Position.Writable<?> == false) {
            return false;
        }
        final List<Object> allElements = new ArrayList<Object>();
        for (Object t : targetIterable) {
            allElements.add(t);
        }
        for (@SuppressWarnings("unused")
        Object s : sourceIterable) {
            allElements.add(null);
        }
        ((Box) targetElements).setValue(allElements.toArray((Object[]) Array.newInstance(rawTargetElementType,
            allElements.size())));

        return context.supports(Convert.to(((Position.Writable<?>) copy.getTargetPosition()), targetElements));
    }

}
