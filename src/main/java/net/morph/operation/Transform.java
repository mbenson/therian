package net.morph.operation;

import net.morph.Operation;
import net.morph.position.Position;

/**
 * Abstract transform operation.
 * A "transformer" is an operator over a transform operation.  Defining "Transformer" in terms of
 * our object model would constrict the behavior of transformer implementations in detrimental ways.
 */
public abstract class Transform<SOURCE, TARGET, RESULT, TARGET_POSITION extends Position<TARGET>> extends Operation<RESULT> {
    private final Position.Readable<SOURCE> sourcePosition;
    private final TARGET_POSITION targetPosition;

    /**
     * Create a new Transform instance.
     * @param sourcePosition
     * @param targetPosition
     */
    protected Transform(Position.Readable<SOURCE> sourcePosition, TARGET_POSITION targetPosition) {
        super();
        this.sourcePosition = sourcePosition;
        this.targetPosition = targetPosition;
    }

    /**
     * Get the sourcePosition.
     * @return Position.Readable<SOURCE>
     */
    public Position.Readable<SOURCE> getSourcePosition() {
        return sourcePosition;
    }

    /**
     * Get the targetPosition.
     * @return TARGET_POSITION
     */
    public TARGET_POSITION getTargetPosition() {
        return targetPosition;
    }

    @Override
    public final RESULT getResult() {
        return super.getResult();
    }
}
