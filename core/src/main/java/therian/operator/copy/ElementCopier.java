package therian.operator.copy;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Iterator;

import therian.Operator;
import therian.TherianContext;
import therian.operation.Convert;
import therian.operation.Copy;
import therian.operation.GetElementType;
import therian.operation.ImmutableCheck;
import therian.operation.Size;
import therian.position.Box;
import therian.position.Position;
import therian.position.Ref;

/**
 * This is a "powerhouse" class which hosts the majority of the code
 * that transforms one set of elements to another. Its mission:
 * 
 * <ul>
 * <li>obtain an iterator from source value</li>
 * <li>obtain element type of target position (responsibility of subclasses)</li>
 * <li>compare size of source and target values:
 * <ol>
 * <li>if target is smaller and immutable, copy existing target elements to new Box of target type and replace target</li>
 * <li>copy source elements onto corresponding target elements</li>
 * <li>when
 * <ol>
 * <li>if yes, copy source elements onto existing target elements; convert and add remainder</li>
 * <li>otherwise, if target position is writable, copy existing target elements
 * </ol>
 * </li>
 * 
 * </ol>
 * </li>
 * </ul>
 */
public class ElementCopier implements Operator<Copy<?, ?>> {

    public void perform(TherianContext context, Copy<?, ?> copy) {
        // TODO Auto-generated method stub

        if (context.eval(ImmutableCheck.of(copy.getTargetPosition())).booleanValue()) {
            // return copy.getTargetPosition() instanceof Position.Writable
            // && context.supports(Convert.to((Position.Writable<?>) copy.getTargetPosition(),
            // copy.getSourcePosition()));
        }

    }

    public boolean supports(TherianContext context, Copy<?, ?> copy) {
        // TODO Auto-generated method stub
        // revisit

        /*
         * pseudocode:
         * 
         * Iterator s = iterator(source);
         * int sourceSize = size(source);
         * int targetSize = size(target);
         */

        final int sourceSize = context.eval(Size.of(copy.getSourcePosition()));
        final int targetSize = context.eval(Size.of(copy.getTargetPosition()));
        
        // do we understand target type as a group of elements?
        final GetElementType<?> getTargetElementType = GetElementType.of(copy.getTargetPosition());
        if (!context.supports(getTargetElementType)) {
            return false;
        }

        final Type targetElementType = context.eval(getTargetElementType);

        @SuppressWarnings("rawtypes")
        final Convert<?, Iterator> sourceToIterator = Convert.to(Iterator.class, copy.getSourcePosition());
        if (!context.supports(sourceToIterator)) {
            return false;
        }
        @SuppressWarnings("rawtypes")
        final Convert<?, Iterator> targetToIterator = Convert.to(Iterator.class, copy.getTargetPosition());

        if (!context.supports(targetToIterator)) {
            return false;
        }
        final Iterator<?> sourceIterator = context.eval(sourceToIterator);
        final Iterator<?> targetIterator = context.eval(targetToIterator);
        
        final Type sourceElementType;
        final GetElementType<?> getSourceElementType = GetElementType.of(copy.getSourcePosition());
        if (context.supports(getSourceElementType)) {
            sourceElementType = context.eval(getSourceElementType);
        } else {
            sourceElementType = null;
        }
        while (targetIterator.hasNext()) {
            if (sourceIterator.hasNext()) {
                final Object targetElement = targetIterator.next();
                final Object sourceElement = sourceIterator.next();
                if (sourceElement != null) {
                    final Position.Readable<?> sourceElementPosition;
                    if (sourceElementType == null || sourceElementType instanceof Class<?>) {
                        sourceElementPosition = Ref.to(sourceElement);
                    } else {
                        sourceElementPosition = new Box<Object>(sourceElementType, sourceElement);
                    }
                    final Position.Readable<?> targetElementPosition;
                    
                    if (targetElement != null && targetElementType instanceof Class<?>) {
                        targetElementPosition = Ref.to(targetElement);
                    }
                }
                continue;
            } else {
                return true;
            }
        }

        if (targetSize < sourceSize && context.eval(ImmutableCheck.of(copy.getTargetPosition())).booleanValue()) {
            // cannot add elements to immutable value...

//            final Box<TARGET> hypotheticalNewTarget = new Box<TARGET>(copy.getTargetPosition().getType());
//            final boolean canCreateEmptyTarget =
//                context.supports(Convert.to(hypotheticalNewTarget, Ref.to(Collections.emptySet())));
//
//            if (!canCreateEmptyTarget) {
//                return false;
//            }

            @SuppressWarnings("rawtypes")
            final Convert<?, Iterable> iterateSource = Convert.to(Iterable.class, copy.getSourcePosition());
            if (!context.supports(iterateSource)) {
                return false;
            }
            for (Object e : context.eval(iterateSource)) {

            }
        }

        if (context.eval(ImmutableCheck.of(copy.getTargetPosition())).booleanValue()) {
            return copy.getTargetPosition() instanceof Position.Writable
                && context.supports(Convert.to((Position.Writable<?>) copy.getTargetPosition(),
                    copy.getSourcePosition()));
        }

        /*
         * how?
         * 
         * if immutable, try a conversion
         */

        return false;
    }

}
