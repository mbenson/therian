package therian.operator.copy;

import java.util.List;

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
    private interface ElementFactory {
        ReadWrite<?> element(int index);
    }

    @Override
    public boolean perform(TherianContext context, Copy<?, ?> copy) {
        final ElementFactory sourceElementFactory = createElementFactory(copy.getSourcePosition());
        final ElementFactory targetElementFactory = createElementFactory(copy.getTargetPosition());
        for (int i = 0, sz = context.eval(Size.of(copy.getSourcePosition())); i < sz; i++) {
            if (context.evalSuccess(Copy.to(targetElementFactory.element(i), sourceElementFactory.element(i)))) {
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

    private static ElementFactory createElementFactory(final Position.Readable<?> source) {
        if (source.getValue() != null) {
            if (TypeUtils.isArrayType(source.getType())) {
                return new ElementFactory() {

                    @Override
                    public ReadWrite<?> element(int index) {
                        return Element.atArrayIndex(index).of(source);
                    }
                };
            }
            if (TypeUtils.isAssignable(source.getType(), Iterable.class)) {

                return new ElementFactory() {

                    @Override
                    @SuppressWarnings("unchecked")
                    public ReadWrite<?> element(int index) {
                        return Element.atIndex(index).of((Position.Readable<Iterable<?>>) source);
                    }
                };
            }
        }
        return null;
    }
}
