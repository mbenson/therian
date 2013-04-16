package therian.operator.copy;

import therian.Operator;
import therian.TherianContext;
import therian.operation.Convert;
import therian.operation.Copy;
import therian.operation.ImmutableCheck;
import therian.position.Position;

public class GenericCollectionCopier implements Operator<Copy<?, ?>> {

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
