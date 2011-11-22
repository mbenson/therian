package uelbox;

import java.util.HashMap;
import java.util.Map;
import javax.el.ValueExpression;
import javax.el.VariableMapper;

/**
 * Simple VariableMapper implementation.
 */
public class SimpleVariableMapper extends VariableMapper {
    private final Map<String, ValueExpression> map = new HashMap<String, ValueExpression>();

    protected boolean containsVariable(String variable) {
        return map.containsKey(variable);
    }

    @Override
    public ValueExpression resolveVariable(String variable) {
        return map.get(variable);
    }

    @Override
    public ValueExpression setVariable(String variable, ValueExpression expression) {
        return map.put(variable, expression);
    }
}
