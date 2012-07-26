package net.morph.operator;

import org.junit.Before;

import net.morph.Morph;
import net.morph.MorphContext;
import net.morph.MorphModule;

public abstract class TransformerTest {

    protected MorphContext morphContext;
    
    @Before
    public void setup() {
        morphContext = Morph.usingModules(modules()).context();
    }

    protected MorphModule[] modules() {
        return new MorphModule[0];
    }

}
