package therian.operator.copy;

import java.util.List;
import java.util.function.IntFunction;

import org.apache.commons.lang3.reflect.TypeUtils;

import therian.Operator.DependsOn;
import therian.TherianContext;
import therian.buildweaver.StandardOperator;
import therian.operation.Copy;
import therian.operation.Size;
import therian.operator.OperatorBase;
import therian.operator.size.DefaultSizeOperator;
import therian.operator.size.SizeOfCollection;
import therian.operator.size.SizeOfIterable;
import therian.position.Position;
import therian.position.Position.ReadWrite;
import therian.position.relative.Element;

/**
 * Tries to copy between arrays/iterables using {@link Element} positions. This should be more efficient than
 * {@link ContainerCopier} where usable. Where elements must be added, only {@link List} targets are supported.
 */
@StandardOperator
@DependsOn({ DefaultSizeOperator.class, SizeOfCollection.class, SizeOfIterable.class })
public class ElementCopier extends OperatorBase<Copy<?, ?>> {

    @SuppressWarnings("unchecked")
    private static IntFunction<ReadWrite<?>> createElementFactory(final Position.Readable<?> source) {
        if (source.getValue() != null) {
            if (TypeUtils.isArrayType(source.getType())) {
                return index -> Element.atArrayIndex(index).of(source);
            }
            if (TypeUtils.isAssignable(source.getType(), Iterable.class)) {
                return index -> Element.atIndex(index).of((Position.Readable<Iterable<?>>) source);
            }
        }
        return null;
    }

    @Override
    public boolean perform(TherianContext context, Copy<?, ?> copy) {
        final IntFunction<ReadWrite<?>> sourceElementFactory = createElementFactory(copy.getSourcePosition());
        final IntFunction<ReadWrite<?>> targetElementFactory = createElementFactory(copy.getTargetPosition());
        for (int i = 0, sz = context.eval(Size.of(copy.getSourcePosition())); i < sz; i++) {
            if (context.evalSuccess(Copy.to(targetElementFactory.apply(i), sourceElementFactory.apply(i)))) {
                continue;
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean supports(TherianContext context, Copy<?, ?> copy) {
        final int sourceSize = context.eval(Size.of(copy.getSourcePosition()));
        final int targetSize = context.eval(Size.of(copy.getTargetPosition()));

        return sourceSize <= targetSize && createElementFactory(copy.getSourcePosition()) != null
            && createElementFactory(copy.getTargetPosition()) != null;
    }
}
