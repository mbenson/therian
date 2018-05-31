package therian.operator.addall;

import java.lang.reflect.Type;

import org.apache.commons.lang3.reflect.TypeUtils;

import therian.Operator.DependsOn;
import therian.TherianContext;
import therian.buildweaver.StandardOperator;
import therian.operation.Add;
import therian.operation.AddAll;
import therian.operation.Convert;
import therian.operation.GetElementType;
import therian.operator.OperatorBase;
import therian.operator.add.AddToCollection;
import therian.operator.add.AddToListIterator;
import therian.operator.convert.DefaultToListConverter;
import therian.operator.convert.EnumerationToList;
import therian.operator.convert.IterableToList;
import therian.position.Position;
import therian.util.Positions;

/**
 * Attempts to convert source to {@link Iterable}, then {@link Add} each element to target. Expressly rejects array
 * targets in favor of {@link AddAllToArray}, which is more efficient for this job.
 */
@StandardOperator
@DependsOn({ AddToCollection.class, AddToListIterator.class, IterableToList.class, EnumerationToList.class,
    DefaultToListConverter.class })
public class GenericAddAllOperator extends OperatorBase<AddAll<?, ?>> {

    @Override
    public boolean perform(TherianContext context, AddAll<?, ?> addAll) {
        final Type sourceElementType = context.eval(GetElementType.of(addAll.getSourcePosition()));

        boolean result = false;

        for (Object o : context.eval(Convert.to(Iterable.class, addAll.getSourcePosition()))) {
            final Position.Readable<Object> sourceElement = Positions.readOnly(sourceElementType, o);
            final Add<Object, ?> add = Add.to(addAll.getTargetPosition(), sourceElement);
            result = context.eval(add).booleanValue() || result;
            if (!add.isSuccessful()) {
                return false;
            }
        }
        addAll.setResult(result);
        return true;
    }

    @Override
    public boolean supports(TherianContext context, AddAll<?, ?> addAll) {
        if (TypeUtils.isArrayType(addAll.getTargetPosition().getType())) {
            return false;
        }
        final GetElementType<?> getSourceElementType = GetElementType.of(addAll.getSourcePosition());
        if (!context.supports(getSourceElementType)) {
            return false;
        }
        @SuppressWarnings("rawtypes")
        final Convert<?, Iterable> toIterable = Convert.to(Iterable.class, addAll.getSourcePosition());

        if (!context.supports(toIterable)) {
            return false;
        }
        final Type sourceElementType = context.eval(getSourceElementType);
        final Position.ReadWrite<Object> sourceElement = Positions.readWrite(sourceElementType);

        for (Object o : context.eval(toIterable)) {
            sourceElement.setValue(o);
            if (!context.supports(Add.to(addAll.getTargetPosition(), sourceElement))) {
                return false;
            }
        }
        return true;
    }
}
