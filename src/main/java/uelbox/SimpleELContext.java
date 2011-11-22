package uelbox;

import java.lang.reflect.Method;
import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.FunctionMapper;
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.ResourceBundleELResolver;
import javax.el.VariableMapper;

/**
 * Simple ELContext implementation.
 */
public class SimpleELContext extends ELContext {
    private final FunctionMapper functionMapper = new FunctionMapper() {
        @Override
        public Method resolveFunction(String prefix, String localName) {
            return null;
        }
    };
    private final VariableMapper variableMapper = new SimpleVariableMapper();
    private final CompositeELResolver elResolver = new CompositeELResolver();
    {
        elResolver.add(new ArrayELResolver(false));
        elResolver.add(new ListELResolver(false));
        elResolver.add(new MapELResolver(false));
        elResolver.add(new ResourceBundleELResolver());
        elResolver.add(new BeanELResolver(false));
    }

    @Override
    public ELResolver getELResolver() {
        return elResolver;
    }

    @Override
    public FunctionMapper getFunctionMapper() {
        return functionMapper;
    }

    @Override
    public VariableMapper getVariableMapper() {
        return variableMapper;
    }
}
