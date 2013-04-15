package therian.operator.addall;

import java.lang.reflect.Type;
import java.util.Iterator;

import therian.Operator;
import therian.TherianContext;
import therian.operation.Add;
import therian.operation.AddAll;
import therian.operation.Convert;
import therian.position.Ref;

public class GenericAddAllOperator implements Operator<AddAll<?, ?>> {
    private static class NullRef extends Ref<Object> {
        private final Type type;

        protected NullRef(Type type) {
            super(null);
            this.type = type;
        }

        @Override
        public Type getType() {
            return type;
        }
    }

    public void perform(AddAll<?, ?> operation) {
        // TODO Auto-generated method stub
        final TherianContext context = TherianContext.getRequiredInstance();
        for (@SuppressWarnings("rawtypes")
        final Iterator iter = context.eval(Convert.to(Iterator.class, operation.getSourcePosition())); iter.hasNext();) {
            final Object element = iter.next();
            // if null, use a raw reference
            final Ref<?> ref = element == null ? new NullRef(null) {} : Ref.to(element);
            context.eval(Add.to(operation.getTargetPosition(), ref));
        }
    }

    public boolean supports(AddAll<?, ?> operation) {
        final TherianContext context = TherianContext.getInstance();
        @SuppressWarnings("rawtypes")
        Convert<?, Iterator> toIterator = Convert.to(Iterator.class, operation.getSourcePosition());
        if (!context.supports(toIterator)) {
            return false;
        }
        // @formatter:off
        for (@SuppressWarnings("rawtypes") final Iterator iter = context.eval(toIterator); iter.hasNext();) {
            // @formatter:on
            final Object element = iter.next();
            // if null, use a raw reference
            @SuppressWarnings({ "rawtypes", "unchecked" })
            final Ref<?> ref = element == null ? new Ref(null) {} : Ref.to(element);
            if (!context.supports(Add.to(operation.getTargetPosition(), ref))) {
                return false;
            }
        }
        return true;
    }

}
